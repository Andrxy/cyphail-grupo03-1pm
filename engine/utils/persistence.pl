:- encoding(utf8).
:- use_module(library(filesex), [make_directory_path/1]).

cy_exec_update(Goal, Headers, Columns, Rows) :-
    with_mutex(cy_graph_persistence,
        transaction((
            findall(Columns, Goal, Solutions),
            format_rows(Headers, Solutions, Rows),
            get_current_graph(G),
            save_graph(G)
        ))).

restore_saved_graphs :-
    forall(graph(G), restore_saved_graph(G)).

restore_saved_graph(G) :-
    state_path(G, Path),
    (   exists_file(Path)
    ->  retractall(node(G, _)),
        retractall(node_label(G, _, _)),
        retractall(node_prop(G, _, _, _)),
        retractall(edge(G, _, _, _)),
        retractall(edge_type(G, _, _)),
        retractall(edge_prop(G, _, _, _)),
        load_files(Path, [silent(true)])
    ;   true
    ).

save_graph(G) :-
    state_directory(Directory),
    make_directory_path(Directory),
    state_path(G, Path),
    current_prolog_flag(pid, Pid),
    format(atom(TemporaryPath), '~w.~d.tmp', [Path, Pid]),
    catch(
        (   setup_call_cleanup(
                open(TemporaryPath, write, Stream, [encoding(utf8)]),
                write_graph(Stream, G),
                close(Stream)
            ),
            rename_file(TemporaryPath, Path)
        ),
        Error,
        (   (exists_file(TemporaryPath) -> delete_file(TemporaryPath) ; true),
            throw(Error)
        )
    ).

state_path(G, Path) :-
    state_directory(Directory),
    atom_concat(G, '.pl', Name),
    directory_file_path(Directory, Name, Path).

state_directory(Directory) :-
    (   getenv('CYPHAIL_STATE_DIR', Directory)
    ->  true
    ;   source_file(cy_exec_query(_, _, _, _), EnginePath),
        file_directory_name(EnginePath, EngineDirectory),
        directory_file_path(EngineDirectory, state, Directory)
    ).

write_graph(Stream, G) :-
    forall(node(G, Id), write_graph_fact(Stream, node(G, Id))),
    forall(node_label(G, Id, Label), write_graph_fact(Stream, node_label(G, Id, Label))),
    forall(node_prop(G, Id, Key, Value), write_graph_fact(Stream, node_prop(G, Id, Key, Value))),
    forall(edge(G, Id, From, To), write_graph_fact(Stream, edge(G, Id, From, To))),
    forall(edge_type(G, Id, Type), write_graph_fact(Stream, edge_type(G, Id, Type))),
    forall(edge_prop(G, Id, Key, Value), write_graph_fact(Stream, edge_prop(G, Id, Key, Value))).

write_graph_fact(Stream, Fact) :-
    write_term(Stream, Fact, [quoted(true), fullstop(true), nl(true)]).
