#!/bin/bash
cd /home/onnury/web
KEYWORD="OnNury-0.0.1-SNAPSHOT.jar"

find_java_svr_pid() {
  PID=$(ps aux | grep "$KEYWORD" | grep -v grep | awk '{print $2}')
}

kill_java_svr() {
  if [ -n "$PID" ]; then
    kill "$PID"
    echo "온누리 몰 프로세스 (PID: $PID)를 종료 후 재실행합니다."
  else
    echo "실행 중인 온누리 몰 프로세스를 찾을 수 없습니다."
  fi
}

find_java_svr_pid
kill_java_svr

nohup java -jar -server -Xmx5g -XX:+UseG1GC -Djava.awt.headless=true -Dspring.profiles.active=dev -Dsvr.nm=ONNURY_DEV -Dfile.encoding=UTF-8 OnNury-0.0.1-SNAPSHOT.jar 1> /dev/null 2>&1 &
