// Copyright (c) 2012, the Dart project authors.  Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

library CountTest;
import '../../pkg/unittest/lib/unittest.dart';
import 'dart:isolate';

void countMessages(replyTo) {
  int count = 0;
  var port = new ReceivePort();
  replyTo.send(["init", port.sendPort]);
  port.listen((int message) {
    if (message == -1) {
      expect(count, 10);
      replyTo.send(["done"]);
      port.close();
      return;
    }
    count++;
    expect(message, count);
    replyTo.send(["count", message * 2]);
  });
}

void main() {
  test("count 10 consecutive messages", () {
    ReceivePort local = new ReceivePort();
    Isolate.spawn(countMessages, local.sendPort);
    SendPort remote;
    int count = 0;
    var done = expectAsync0((){});
    local.listen((msg) {
      switch (msg[0]) {
        case "init":
          expect(remote, null);
          remote = msg[1];
          remote.send(++count);
          break;
        case "count":
          expect(msg[1], count * 2);
          if (count == 10) {
            remote.send(-1);
          } else {
            remote.send(++count);
          }
          break;
        case "done":
          expect(count, 10);
          local.close();
          done();
          break;
        default:
          fail("unreachable: ${msg[0]}");
      }
    });
  });
}
