/*
 * Copyright (c) 2012, the Dart project authors.
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
package com.google.dart.tools.core.generator;

import java.util.ArrayList;
import java.util.List;

/**
 * Create a sample Chrome packaged application.
 * 
 * @coverage dart.tools.core.generator
 */
public class ChromePackagedAppSample extends AbstractSample {

  public ChromePackagedAppSample() {
    super("Chrome packaged application", "Create a sample Chrome packaged application");

    List<String[]> templates = new ArrayList<String[]>();

    templates.add(new String[] {"build.dart", "@chrome/build.dart"});
    templates.add(new String[] {
        "pubspec.yaml",
        "name: {name}\ndescription: A sample chrome packaged application\n"
            + "dependencies:\n  browser: any\n"});
    templates.add(new String[] {"README.md", "@chrome/readme.md"});

    templates.add(new String[] {"app/dart_icon.png", "@chrome/dart_icon.png"});
    templates.add(new String[] {"app/background.js", "@chrome/background.js"});
    templates.add(new String[] {"app/bootstrap.js", "@chrome/bootstrap.js"});
    templates.add(new String[] {"app/manifest.json", "@chrome/manifest.json"});
    templates.add(new String[] {"app/{name.lower}.dart", "@chrome/sample.dart"});
    templates.add(new String[] {"app/{name.lower}.html", "@chrome/sample.html"});
    templates.add(new String[] {"app/{name.lower}.css", "@chrome/sample.css"});

    setTemplates(templates);
    setMainFile("app/manifest.json");
  }

}
