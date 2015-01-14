# JHttpLoad
---
a simple Java version of [http_load][],implemented with Java NIO.
[http_load]: https://github.com/timbunce/http_load](https://github.com/timbunce/http_load

http_load is a c implemented http benchmark program.

>http_load runs multiple http fetches in parallel, to test the
>throughput of a web server.  However unlike most such test clients,
>it runs in a single process, so it doesn't bog down the client
>machine.  It can be configured to do https fetches as well.
>JHttpLoad almost has the same  features with http_load.

JHttpLoad almost has the same features with http_load.

## Usage

~~~
usage: jhttpload -parallel N | -rate N 
 -fetches N | -seconds N 
 [-timeout seconds] url_file
usage: Main
 -f,--fetches <arg>    fetch times for every url.either -seconds or
                       -fetches is required.
 -h,--help             show usage.
 -p,--parallel <arg>   parallel mode,the program will test every url with
                       specific number of connections.
                       either -rate or -parallel is required.
 -r,--rate <arg>       rate mode,the program will test every url at
                       specific rate,like 2 fetches per second.
                       either -rate or -parallel is required.
 -s,--seconds <arg>    test last time(seconds).either -seconds or -fetches
                       is required.
 -timeout <arg>        request timout(seconds).not required,default 10
                       seconds.
~~~

## Results comare

**load test url.txt**:
~~~
http://123.57.76.162/blog/
~~~

**JHttpLoad**:
~~~
18 fetches, 4 max parallel, 91998 bytes, in 10.0000 seconds
5111.00  mean bytes/connection
1.80000 fetches/sec, 9199.80 bytes/sec
msecs/connect: 114.944 mean, 133 max, 105 min
msecs/first-response: 332.611 mean, 1474 max, 240 min
0 timeouts
~~~

**http_load**:
~~~
18 fetches, 2 max parallel, 86724 bytes, in 10.0006 seconds
4818 mean bytes/connection
1.7999 fetches/sec, 8671.9 bytes/sec
msecs/connect: 114.507 mean, 129.007 max, 102.006 min
msecs/first-response: 233.013 mean, 260.014 max, 215.012 min
HTTP response codes:
  code 200 -- 18
~~~





