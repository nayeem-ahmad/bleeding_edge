// Copyright (c) 2013, the Dart project authors.  Please see the AUTHORS d.file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

library pub_tests;

import '../descriptor.dart' as d;
import '../test_pub.dart';
import '../serve/utils.dart';

const WRONG_TRANSFORMER = """
import 'dart:async';

import 'package:barback/barback.dart';

class RewriteTransformer extends Transformer {
  RewriteTransformer.asPlugin();

  String get allowedExtensions => '.txt';

  Future apply(Transform transform) {
    return transform.primaryInput.readAsString().then((contents) {
      var id = transform.primaryInput.id.changeExtension(".wrong");
      transform.addOutput(new Asset.fromString(id, "\$contents.wrong"));
    });
  }
}
""";

main() {
  initConfig();
  integration("prefers transformer.dart to <package name>.dart", () {
    d.dir(appPath, [
      d.pubspec({
        "name": "myapp",
        "transformers": ["myapp"]
      }),
      d.dir("lib", [
        d.file("transformer.dart", REWRITE_TRANSFORMER),
        d.file("myapp.dart", WRONG_TRANSFORMER)
      ]),
      d.dir("web", [
        d.file("foo.txt", "foo")
      ])
    ]).create();

    createLockFile('myapp', pkg: ['barback']);

    pubServe();
    requestShouldSucceed("foo.out", "foo.out");
    requestShould404("foo.wrong");
    endPubServe();
  });
}
