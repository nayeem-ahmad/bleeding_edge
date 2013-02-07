// Copyright (c) 2013, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file

library cdata_test;
import '../../pkg/unittest/lib/unittest.dart';
import '../../pkg/unittest/lib/html_config.dart';
import 'dart:html';


main() {
  test('remove', () {
    var div = new Element.html('<div>content</div>');
    var cdata = div.nodes[0];
    expect(cdata is CharacterData, true);
    expect(cdata, isNotNull);
    expect(div.innerHtml, 'content');

    cdata.remove();
    expect(div.innerHtml, '');
  });
}
