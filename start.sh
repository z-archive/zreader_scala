#!/bin/sh
erl -pa ebin deps/*/ebin -s zreader \
	-eval "io:format(\"Point your browser at http://localhost:8080/test.txt~n\")." \
	-eval "io:format(\"Point your browser at http://localhost:8080/video.html~n\")."
