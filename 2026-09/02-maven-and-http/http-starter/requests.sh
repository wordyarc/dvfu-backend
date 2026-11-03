#!/usr/bin/env bash

BASE="${BASE:-http://127.0.0.1:8080}"
TMP="$(mktemp)"
PASS=0
FAIL=0

check() {
  local expected="$1" method="$2" path="$3" code
  if [ $# -ge 4 ]; then
    code=$(curl -s -o "$TMP" -w '%{http_code}' -X "$method" "$BASE$path" \
      -H 'Content-Type: application/json' -d "$4")
  else
    code=$(curl -s -o "$TMP" -w '%{http_code}' -X "$method" "$BASE$path")
  fi
  if [ "$code" = "$expected" ]; then
    PASS=$((PASS + 1)); status="OK  "
  else
    FAIL=$((FAIL + 1)); status="FAIL"
  fi
  echo "[$status] ожидалось $expected, получено $code: $method $path ${4-}"
  echo "         $(cat "$TMP")"
}

section() { echo; echo "=== $1 ==="; }

section "ping"
check 200 GET  /ping
check 405 POST /ping

section "Клиенты: создание (id 1..4)"
check 201 POST /members '{"type":"REGULAR","name":"Ivan","age":25}'
check 201 POST /members '{"type":"VIP","name":"Anna","age":30}'
check 201 POST /members '{"type":"SILVER","name":"Limit","age":40}'
check 201 POST /members '{"type":"GOLD","name":"Gold","age":35}'

section "Клиенты: ошибки создания"
check 400 POST /members '{"type":"GOLD","name":"","age":25}'
check 400 POST /members '{"type":"GOLD","name":"   ","age":25}'
check 400 POST /members '{"type":"GOLD","age":25}'
check 400 POST /members '{"type":"GOLD","name":"Bob","age":17}'
check 400 POST /members '{"type":"GOLD","name":"Bob","age":121}'
check 400 POST /members '{"type":"GOLD","name":"Bob"}'
check 400 POST /members '{"name":"Bob","age":25}'
check 400 POST /members '{"type":"PLATINUM","name":"Bob","age":25}'
check 400 POST /members '{"type":"GOLD","name":"Bob","age":"abc"}'
check 400 POST /members '{not json'
check 400 POST /members 'null'
check 400 POST /members ''

section "Клиенты: получение"
check 200 GET /members/1
check 404 GET /members/999
check 404 GET /members/abc
check 404 GET /members/1/foo
check 404 GET /membersx

section "Клиенты: список и фильтры"
check 200 GET "/members"
check 200 GET "/members?type=vip"
check 200 GET "/members?type=SILVER"
check 200 GET "/members?name=iv"
check 200 GET "/members?canBook=true"
check 200 GET "/members?type=silver&canBook=true&name=lim"
check 400 GET "/members?type=abc"
check 400 GET "/members?type="
check 400 GET "/members?canBook=maybe"
check 400 GET "/members?foo=1"

section "Клиенты: изменение"
check 200 PUT /members/1 '{"type":"REGULAR","name":"Ivan P","age":26}'
check 200 PUT /members/1 '{"name":"Ivan","age":25}'
check 400 PUT /members/1 '{"type":"VIP","name":"Ivan","age":25}'
check 400 PUT /members/1 '{"name":"Ivan"}'
check 400 PUT /members/1 '{"name":"","age":25}'
check 400 PUT /members/1 '{"name":"Ivan","age":5}'
check 400 PUT /members/1 '{bad'
check 404 PUT /members/999 '{"name":"X","age":25}'

section "Клиенты: неподходящие методы"
check 405 PATCH  /members
check 405 DELETE /members
check 405 POST   /members/1
check 405 POST   /members/1/sessions

section "Занятия: создание (id 1 и 2)"
check 201 POST /sessions '{"name":"Yoga","capacity":1}'
check 201 POST /sessions '{"name":"Pilates","capacity":10}'

section "Занятия: ошибки создания"
check 400 POST /sessions '{"name":"","capacity":5}'
check 400 POST /sessions '{"capacity":5}'
check 400 POST /sessions '{"name":"Box","capacity":0}'
check 400 POST /sessions '{"name":"Box","capacity":501}'
check 400 POST /sessions '{"name":"Box"}'
check 400 POST /sessions '{not json'
check 400 POST /sessions ''

section "Занятия: получение, список, фильтры"
check 200 GET /sessions/1
check 404 GET /sessions/999
check 404 GET /sessions/abc
check 404 GET /sessions/1/foo
check 200 GET "/sessions"
check 200 GET "/sessions?hasFreePlaces=true"
check 200 GET "/sessions?name=yo"
check 400 GET "/sessions?hasFreePlaces=x"
check 400 GET "/sessions?foo=1"

section "Занятия: изменение"
check 200 PUT /sessions/2 '{"name":"Pilates 2","capacity":10}'
check 400 PUT /sessions/2 '{"name":"","capacity":10}'
check 400 PUT /sessions/2 '{"name":"Pilates","capacity":0}'
check 400 PUT /sessions/2 '{"name":"Pilates"}'
check 404 PUT /sessions/999 '{"name":"X","capacity":5}'

section "Занятия: неподходящие методы"
check 405 PATCH /sessions
check 405 GET   /sessions/1/book
check 405 GET   /sessions/1/cancel
check 405 POST  /sessions/1/members

section "Запись на занятие"
check 200 POST /sessions/1/book '{"memberId":1}'
check 409 POST /sessions/1/book '{"memberId":1}'
check 409 POST /sessions/1/book '{"memberId":2}'
check 400 POST /sessions/1/book '{}'
check 400 POST /sessions/1/book '{bad'
check 404 POST /sessions/1/book '{"memberId":999}'
check 404 POST /sessions/999/book '{"memberId":1}'
check 200 POST /sessions/2/book '{"memberId":1}'
check 200 POST /sessions/2/book '{"memberId":2}'

section "Связи"
check 200 GET /sessions/1/members
check 200 GET /sessions/2/members
check 200 GET /members/1/sessions
check 404 GET /members/999/sessions
check 404 GET /sessions/999/members
check 200 GET "/sessions?hasFreePlaces=false"
check 200 GET /members/1

section "Правила предметной области при изменении и удалении"
check 409 PUT    /sessions/2 '{"name":"Pilates","capacity":1}'
check 409 DELETE /members/1
check 409 DELETE /sessions/1

section "Отмена записи"
check 409 POST /sessions/1/cancel '{"memberId":2}'
check 400 POST /sessions/1/cancel '{}'
check 404 POST /sessions/999/cancel '{"memberId":1}'
check 404 POST /sessions/1/cancel '{"memberId":999}'
check 200 POST /sessions/1/cancel '{"memberId":1}'
check 200 GET  /members/1

section "Лимит абонемента SILVER (8 занятий), клиент 3"
for i in 3 4 5 6 7 8 9 10 11; do
  check 201 POST /sessions "{\"name\":\"S$i\",\"capacity\":10}"
done
for i in 3 4 5 6 7 8 9 10; do
  check 200 POST /sessions/$i/book '{"memberId":3}'
done
check 409 POST /sessions/11/book '{"memberId":3}'
check 200 GET  /members/3
check 200 GET  "/members?canBook=false"
check 200 POST /sessions/3/cancel '{"memberId":3}'
check 200 POST /sessions/11/book '{"memberId":3}'

section "Удаление"
check 204 DELETE /sessions/1
check 404 GET    /sessions/1
check 404 DELETE /sessions/1
check 200 POST   /sessions/2/cancel '{"memberId":1}'
check 200 POST   /sessions/2/cancel '{"memberId":2}'
check 204 DELETE /sessions/2
check 204 DELETE /members/1
check 404 GET    /members/1
check 404 DELETE /members/1

rm -f "$TMP"
echo
echo "Итого: успешно $PASS, ошибок $FAIL"
[ "$FAIL" -eq 0 ]