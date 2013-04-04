%% Feel free to use, reuse and abuse the code in this file.

%% @private
-module(zreader_app).
-behaviour(application).

%% API.
-export([start/2]).
-export([stop/1]).

%% API.

start(_Type, _Args) ->
    %%    Routes = [Host1, Host2, ... HostN].
    Host = '_',
    Index = { "/", cowboy_static, [
				     {directory, {priv_dir, zreader, []}},
				     {file, <<"index.html">>},
				     {mimetypes, {fun mimetypes:path_to_mimes/2, default}}
				    ]},
    Dispatch = cowboy_router:compile([ { Host, [Index] } ]),
    {ok, _} = cowboy:start_http(http, 100, [{port, 8080}], [
							    {env, [{dispatch, Dispatch}]}
							   ]),
    zreader_sup:start_link().
stop(_State) ->
    ok.
