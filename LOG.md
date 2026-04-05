&#x20;=== LOGIN RESPONSE ===

2026-04-05 13:51:37.530 19436-19436 AuthApiRepository       com.example.testapp                  D  Response code: 401

2026-04-05 13:51:37.530 19436-19436 AuthApiRepository       com.example.testapp                  D  Response message: Unauthorized

2026-04-05 13:51:37.530 19436-19436 AuthApiRepository       com.example.testapp                  E  Error response: 401 - Invalid email or password

2026-04-05 13:52:14.469 19436-19436 AuthApiRepository       com.example.testapp                  D  === REGISTER REQUEST ===

2026-04-05 13:52:14.469 19436-19436 AuthApiRepository       com.example.testapp                  D  Name: name

2026-04-05 13:52:14.469 19436-19436 AuthApiRepository       com.example.testapp                  D  Email: name@email.com

2026-04-05 13:52:14.469 19436-19436 AuthApiRepository       com.example.testapp                  D  Password length: 8

2026-04-05 13:52:14.513 19436-19436 AuthApiRepository       com.example.testapp                  D  === REGISTER RESPONSE ===

2026-04-05 13:52:14.513 19436-19436 AuthApiRepository       com.example.testapp                  D  Response code: 201

2026-04-05 13:52:14.513 19436-19436 AuthApiRepository       com.example.testapp                  D  Response body: UserResponse(id=5, email=name@email.com, name=name, role=User)

2026-04-05 13:52:14.514 19436-19436 AuthApiRepository       com.example.testapp                  D  User registered: name, id: 5

2026-04-05 13:52:14.514 19436-19436 AuthApiRepository       com.example.testapp                  D  === LOGIN REQUEST ===

2026-04-05 13:52:14.514 19436-19436 AuthApiRepository       com.example.testapp                  D  Email: name@email.com

2026-04-05 13:52:14.514 19436-19436 AuthApiRepository       com.example.testapp                  D  Password length: 8

2026-04-05 13:52:14.582 19436-19436 AuthApiRepository       com.example.testapp                  E  Network error: Field 'user' is required for type with serial name 'com.example.testapp.data.api.model.UserLoginResponse', but it was missing at path: $


2026-04-05 13:51:37.464 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  --> POST http://10.0.2.2:8080/login

2026-04-05 13:51:37.465 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  Content-Type: application/json; charset=utf-8

2026-04-05 13:51:37.465 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  Content-Length: 49

2026-04-05 13:51:37.465 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  {"email":"email@email.com","password":"12345678"}

2026-04-05 13:51:37.465 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  --> END POST (49-byte body)

2026-04-05 13:51:37.512 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  <-- 401 Unauthorized http://10.0.2.2:8080/login (47ms)

2026-04-05 13:51:37.513 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  Content-Length: 25

2026-04-05 13:51:37.513 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  Content-Type: text/plain; charset=UTF-8

2026-04-05 13:51:37.513 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  Connection: keep-alive

2026-04-05 13:51:37.514 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  Invalid email or password

2026-04-05 13:51:37.514 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  <-- END HTTP (25-byte body)

2026-04-05 13:52:14.473 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  --> POST http://10.0.2.2:8080/register

2026-04-05 13:52:14.473 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  Content-Type: application/json; charset=utf-8

2026-04-05 13:52:14.473 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  Content-Length: 62

2026-04-05 13:52:14.473 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  {"email":"name@email.com","name":"name","password":"12345678"}

2026-04-05 13:52:14.473 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  --> END POST (62-byte body)

2026-04-05 13:52:14.511 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  <-- 201 Created http://10.0.2.2:8080/register (37ms)

2026-04-05 13:52:14.511 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  Content-Length: 61

2026-04-05 13:52:14.511 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  Content-Type: application/json

2026-04-05 13:52:14.511 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  Connection: keep-alive

2026-04-05 13:52:14.512 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  {"id":5,"email":"name@email.com","name":"name","role":"User"}

2026-04-05 13:52:14.512 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  <-- END HTTP (61-byte body)

2026-04-05 13:52:14.515 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  --> POST http://10.0.2.2:8080/login

2026-04-05 13:52:14.515 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  Content-Type: application/json; charset=utf-8

2026-04-05 13:52:14.515 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  Content-Length: 48

2026-04-05 13:52:14.516 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  {"email":"name@email.com","password":"12345678"}

2026-04-05 13:52:14.516 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  --> END POST (48-byte body)

2026-04-05 13:52:14.580 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  <-- 200 OK http://10.0.2.2:8080/login (63ms)

2026-04-05 13:52:14.580 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  Content-Length: 48

2026-04-05 13:52:14.580 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  Content-Type: application/json

2026-04-05 13:52:14.580 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  Connection: keep-alive

2026-04-05 13:52:14.581 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  {"token":"632ec902-8139-4b59-a566-2b5698d0feae"}

2026-04-05 13:52:14.581 19436-19512 okhttp.OkHttpClient     com.example.testapp                  I  <-- END HTTP (48-byte body)

