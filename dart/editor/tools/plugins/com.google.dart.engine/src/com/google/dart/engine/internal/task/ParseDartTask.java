/*
 * Copyright (c) 2013, the Dart project authors.
 * 
 * Licensed under the Eclipse Public License v1.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.dart.engine.internal.task;

import com.google.dart.engine.ast.CompilationUnit;
import com.google.dart.engine.ast.Directive;
import com.google.dart.engine.ast.ExportDirective;
import com.google.dart.engine.ast.ImportDirective;
import com.google.dart.engine.ast.LibraryDirective;
import com.google.dart.engine.ast.PartDirective;
import com.google.dart.engine.ast.PartOfDirective;
import com.google.dart.engine.ast.StringInterpolation;
import com.google.dart.engine.ast.StringLiteral;
import com.google.dart.engine.ast.UriBasedDirective;
import com.google.dart.engine.context.AnalysisException;
import com.google.dart.engine.error.AnalysisError;
import com.google.dart.engine.internal.context.InternalAnalysisContext;
import com.google.dart.engine.internal.context.PerformanceStatistics;
import com.google.dart.engine.internal.context.RecordingErrorListener;
import com.google.dart.engine.parser.Parser;
import com.google.dart.engine.scanner.CharSequenceReader;
import com.google.dart.engine.scanner.Scanner;
import com.google.dart.engine.scanner.Token;
import com.google.dart.engine.source.Source;
import com.google.dart.engine.utilities.general.TimeCounter.TimeCounterHandle;
import com.google.dart.engine.utilities.io.UriUtilities;
import com.google.dart.engine.utilities.source.LineInfo;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.CharBuffer;
import java.util.HashSet;

/**
 * Instances of the class {@code ParseDartTask} parse a specific source as a Dart file.
 */
public class ParseDartTask extends AnalysisTask {
  /**
   * The source to be parsed.
   */
  private Source source;

  /**
   * The time at which the contents of the source were last modified.
   */
  private long modificationTime = -1L;

  /**
   * The line information that was produced.
   */
  private LineInfo lineInfo;

  /**
   * The compilation unit that was produced by parsing the source.
   */
  private CompilationUnit unit;

  /**
   * The errors that were produced by scanning and parsing the source.
   */
  private AnalysisError[] errors = AnalysisError.NO_ERRORS;

  /**
   * A flag indicating whether the source contains a 'part of' directive.
   */
  private boolean hasPartOfDirective = false;

  /**
   * A flag indicating whether the source contains a 'library' directive.
   */
  private boolean hasLibraryDirective = false;

  /**
   * A set containing the sources referenced by 'export' directives.
   */
  private HashSet<Source> exportedSources = new HashSet<Source>();

  /**
   * A set containing the sources referenced by 'import' directives.
   */
  private HashSet<Source> importedSources = new HashSet<Source>();

  /**
   * A set containing the sources referenced by 'part' directives.
   */
  private HashSet<Source> includedSources = new HashSet<Source>();

  /**
   * Initialize a newly created task to perform analysis within the given context.
   * 
   * @param context the context in which the task is to be performed
   * @param source the source to be parsed
   */
  public ParseDartTask(InternalAnalysisContext context, Source source) {
    super(context);
    this.source = source;
  }

  @Override
  public <E> E accept(AnalysisTaskVisitor<E> visitor) throws AnalysisException {
    return visitor.visitParseDartTask(this);
  }

  /**
   * Return the compilation unit that was produced by parsing the source, or {@code null} if the
   * task has not yet been performed or if an exception occurred.
   * 
   * @return the compilation unit that was produced by parsing the source
   */
  public CompilationUnit getCompilationUnit() {
    return unit;
  }

  /**
   * Return the errors that were produced by scanning and parsing the source, or {@code null} if the
   * task has not yet been performed or if an exception occurred.
   * 
   * @return the errors that were produced by scanning and parsing the source
   */
  public AnalysisError[] getErrors() {
    return errors;
  }

  /**
   * Return an array containing the sources referenced by 'export' directives, or an empty array if
   * the task has not yet been performed or if an exception occurred.
   * 
   * @return an array containing the sources referenced by 'export' directives
   */
  public Source[] getExportedSources() {
    return toArray(exportedSources);
  }

  /**
   * Return an array containing the sources referenced by 'import' directives, or an empty array if
   * the task has not yet been performed or if an exception occurred.
   * 
   * @return an array containing the sources referenced by 'import' directives
   */
  public Source[] getImportedSources() {
    return toArray(importedSources);
  }

  /**
   * Return an array containing the sources referenced by 'part' directives, or an empty array if
   * the task has not yet been performed or if an exception occurred.
   * 
   * @return an array containing the sources referenced by 'part' directives
   */
  public Source[] getIncludedSources() {
    return toArray(includedSources);
  }

  /**
   * Return the line information that was produced, or {@code null} if the task has not yet been
   * performed or if an exception occurred.
   * 
   * @return the line information that was produced
   */
  public LineInfo getLineInfo() {
    return lineInfo;
  }

  /**
   * Return the time at which the contents of the source that was parsed were last modified, or a
   * negative value if the task has not yet been performed or if an exception occurred.
   * 
   * @return the time at which the contents of the source that was parsed were last modified
   */
  public long getModificationTime() {
    return modificationTime;
  }

  /**
   * Return the source that is to be parsed.
   * 
   * @return the source to be parsed
   */
  public Source getSource() {
    return source;
  }

  /**
   * Return {@code true} if the source contains a 'library' directive, or {@code false} if the task
   * has not yet been performed or if an exception occurred.
   * 
   * @return {@code true} if the source contains a 'library' directive
   */
  public boolean hasLibraryDirective() {
    return hasLibraryDirective;
  }

  /**
   * Return {@code true} if the source contains a 'part of' directive, or {@code false} if the task
   * has not yet been performed or if an exception occurred.
   * 
   * @return {@code true} if the source contains a 'part of' directive
   */
  public boolean hasPartOfDirective() {
    return hasPartOfDirective;
  }

  @Override
  protected String getTaskDescription() {
    if (source == null) {
      return "parse as dart null source";
    }
    return "parse as dart " + source.getFullName();
  }

  @Override
  protected void internalPerform() throws AnalysisException {
    final RecordingErrorListener errorListener = new RecordingErrorListener();
    final Token[] token = {null};
    //
    // Scan the contents of the file.
    //
    TimeCounterHandle timeCounterScan = PerformanceStatistics.scan.start();
    try {
      Source.ContentReceiver receiver = new Source.ContentReceiver() {
        @Override
        public void accept(CharBuffer contents, long modificationTime) {
          ParseDartTask.this.modificationTime = modificationTime;
          Scanner scanner = new Scanner(source, new CharSequenceReader(contents), errorListener);
          token[0] = scanner.tokenize();
          lineInfo = new LineInfo(scanner.getLineStarts());
        }

        @Override
        public void accept(String contents, long modificationTime) {
          ParseDartTask.this.modificationTime = modificationTime;
          Scanner scanner = new Scanner(source, new CharSequenceReader(contents), errorListener);
          token[0] = scanner.tokenize();
          lineInfo = new LineInfo(scanner.getLineStarts());
        }
      };
      try {
        source.getContents(receiver);
      } catch (Exception exception) {
        modificationTime = source.getModificationStamp();
        throw new AnalysisException(exception);
      }
    } finally {
      timeCounterScan.stop();
    }
    //
    // Then parse the token stream.
    //
    TimeCounterHandle timeCounterParse = PerformanceStatistics.parse.start();
    try {
      Parser parser = new Parser(source, errorListener);
      unit = parser.parseCompilationUnit(token[0]);
      errors = errorListener.getErrors(source);
      for (Directive directive : unit.getDirectives()) {
        if (directive instanceof ExportDirective) {
          Source exportSource = resolveSource(source, (ExportDirective) directive);
          if (exportSource != null) {
            exportedSources.add(exportSource);
          }
        } else if (directive instanceof ImportDirective) {
          Source importSource = resolveSource(source, (ImportDirective) directive);
          if (importSource != null) {
            importedSources.add(importSource);
          }
        } else if (directive instanceof LibraryDirective) {
          hasLibraryDirective = true;
        } else if (directive instanceof PartDirective) {
          Source partSource = resolveSource(source, (PartDirective) directive);
          if (partSource != null) {
            includedSources.add(partSource);
          }
        } else if (directive instanceof PartOfDirective) {
          hasPartOfDirective = true;
        }
      }
      unit.setLineInfo(lineInfo);
    } finally {
      timeCounterParse.stop();
    }
  }

  /**
   * Return the result of resolving the URI of the given URI-based directive against the URI of the
   * given library, or {@code null} if the URI is not valid.
   * 
   * @param librarySource the source representing the library containing the directive
   * @param directive the directive which URI should be resolved
   * @return the result of resolving the URI against the URI of the library
   */
  private Source resolveSource(Source librarySource, UriBasedDirective directive) {
    StringLiteral uriLiteral = directive.getUri();
    if (uriLiteral instanceof StringInterpolation) {
      return null;
    }
    String uriContent = uriLiteral.getStringValue().trim();
    if (uriContent == null) {
      return null;
    }
    uriContent = UriUtilities.encode(uriContent);
    try {
      new URI(uriContent);
      return getContext().getSourceFactory().resolveUri(librarySource, uriContent);
    } catch (URISyntaxException exception) {
      return null;
    }
  }

  /**
   * Efficiently convert the given set of sources to an array.
   * 
   * @param sources the set to be converted
   * @return an array containing all of the sources in the given set
   */
  private Source[] toArray(HashSet<Source> sources) {
    int size = sources.size();
    if (size == 0) {
      return Source.EMPTY_ARRAY;
    }
    return sources.toArray(new Source[size]);
  }
}
