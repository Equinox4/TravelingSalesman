<?php
/**
 * Created by DUTRA Enzo.
 * Date: 05/03/2021
 * Time: 11:52
 */
try{
    $bdd = new PDO('mysql:host=db5001891077.hosting-data.io;dbname=dbs1550468;charset=utf8', 'dbu1547867', '0n-V4-F4ir3-Mi3ux-Qu-4ndr3');
} catch (Exception $e) {
    die('Erreur : ' . $e->getMessage());
}

$response = "ERROR";

if (isset($_POST['new_graph'])
    && isset($_POST['points'])
    && isset($_POST['hit_points'])) {
    // input  : graph
    // output : graph id

    // pour qu'on ne puisse pas envoyer plusieurs fois le meme graphe
    $hash = md5($_POST['points'] . $_POST['hit_points']);

    if(!row_exists_in($bdd, "hash", $hash, "graph")){
        // save graph
        $stmt = $bdd->prepare('INSERT INTO graph (hash, points, hit_points) VALUES (?, ?, ?)');
        $stmt->bindParam(1, $hash, PDO::PARAM_STR);
        $stmt->bindParam(2, $_POST['points'], PDO::PARAM_STR);
        $stmt->bindParam(3, $_POST['hit_points'], PDO::PARAM_STR);
        $stmt->execute();

        // get graph id
        $result = select_a_from_b_where_c_is_d($bdd, "id", "graph", "hash", $hash);
        $id = $result->fetch(PDO::FETCH_ASSOC)["id"];

        $response = $id;
    } else {
        $response = "ALREADY_PRESENT";
    }
} else if (isset($_POST['new_solution'])
    && isset($_POST['id_graph'])
    && isset($_POST['ordered_hit_points'])
    && isset($_POST['score'])) {
    // input  : solution
    // output : solution id

    if(row_exists_in($bdd, "id", $_POST['id_graph'], "graph")){
        // pour qu'on ne puisse pas envoyer plusieurs fois la meme solution
        $hash = md5($_POST['id_graph'] . $_POST['ordered_hit_points']);
        if(!row_exists_in($bdd, "hash", $hash, "solutions")) {

            $stmt = $bdd->prepare('INSERT INTO solutions (hash, id_graph, ordered_hit_points, score) VALUES (?, ?, ?, ?)');
            $stmt->bindParam(1, $hash, PDO::PARAM_STR);
            $stmt->bindParam(2, $_POST['id_graph'], PDO::PARAM_STR);
            $stmt->bindParam(3, $_POST['ordered_hit_points'], PDO::PARAM_STR);
            $stmt->bindParam(4, $_POST['score'], PDO::PARAM_STR);
            $res = $stmt->execute();

            // get solution id
            $result = select_a_from_b_where_c_is_d($bdd, "id", "solutions", "hash", $hash);
            $id = $result->fetch(PDO::FETCH_ASSOC)["id"];

            $response = $id;
        } else {
            $response = "ALREADY_PRESENT";
        }
    } else {
        $response = "GRAPH_DOESNT_EXISTS";
    }
} else if (isset($_GET['delete_solution'])
    && isset($_GET['id_graph'])
    && isset($_GET['score'])) {
    // input  : solution

    // On met juste l'ID en negatif comme ça tout est reccupérable en cas d'erreur de suppression
    $stmt = $bdd->prepare('UPDATE solutions SET id_graph = (0-'.$_GET['id_graph'].') WHERE id_graph = '.$_GET['id_graph'].' AND score = '.$_GET['score']);

    $res = $stmt->execute();

    if($res){
        $response = "200"; // succes (il faut un int)
    } else {
        $response = "SOLUTION_DOESNT_EXISTS";
    }
} else if (isset($_GET['solutions'])
    && isset($_GET['best_of_each'])) {
    $response = "";

    // on recherche la meilleure solution pour chaque graphe
    $sql = "SELECT id FROM graph ORDER BY id"; // SQL with parameters
    foreach  ($bdd->query($sql) as $row) {
        $stmt = $bdd->prepare('SELECT ordered_hit_points FROM solutions WHERE id_graph = ? ORDER BY ASC score LIMIT 1');
        $stmt->bindParam(1, $row['id'], PDO::PARAM_STR);
        $stmt->execute();
        $sol = $stmt->fetch(PDO::FETCH_ASSOC)["ordered_hit_points"];

        $response .= "\nID=" . $row['id'] . "\n" . $sol;
    }
} else if (isset($_GET['solutions'])
    && isset($_GET['n_best_of_one'])
    && isset($_GET['id_graph'])
    && isset($_GET['n'])) {
    $response = "";

    // on recherche les n meilleurs pour ce graphe
    $stmt = $bdd->prepare('
        SELECT ordered_hit_points 
        FROM solutions 
        WHERE id_graph = '.$_GET['id_graph'].' 
        ORDER BY score ASC LIMIT '.$_GET['n'].'
    ');

    if($stmt->execute()){
        $tab = [];
        foreach($stmt->fetchAll(PDO::FETCH_ASSOC) as $res){
            array_push($tab, str_replace("\n", "-", $res["ordered_hit_points"]));
        }
        $tmp = implode("/" , $tab);
        $response = str_replace("\n", "-", $tmp);
    } else {
        $response = "GRAPH_SOLUTION_DOESNT_EXISTS";
    }
} else if (isset($_GET['solutions'])
    && isset($_GET['best_of_hash'])) {
    $response = "";

    // on recherche le meilleur pour ce graphe
    //$stmt = $bdd->prepare('SELECT s.ordered_hit_points as points FROM graph g, solutions s WHERE g.hash = ? AND g.id = s.id_graph ORDER BY s.score ASC LIMIT 1');
    $stmt = $bdd->prepare('
        select sol.ordered_hit_points as points
        from solutions sol, 
        (select g.id as id_graph, min(s.score) as score from solutions s, graph g where s.id_graph = g.id and g.hash = ? group by s.id_graph) t
        where t.id_graph = sol.id_graph
        and sol.score = t.score
    ');
    $stmt->bindParam(1, $_GET['best_of_hash']);

    if($stmt->execute()){
        $result = $stmt->fetch(PDO::FETCH_ASSOC);
        $points = str_replace("\n", "-", $result["points"]);

        $response .= $points;
    } else {
        $response = "GRAPH_SOLUTION_DOESNT_EXISTS";
    }
} else if (isset($_GET['missing_solutions'])) {
    $response = "";

    $sql = "SELECT id FROM graph WHERE id NOT IN (SELECT id_graph FROM solutions)";
    foreach  ($bdd->query($sql) as $result) {
        $response .= "ID=" . $result["id"] . " ";
    }
} else if (isset($_GET['get_graph_to_improve'])
    && isset($_GET['top_to_keep'])) {
    $response = "";

    $sql = "
        SELECT 
               graphe.id, 
               graphe.points, 
               sol.ordered_hit_points
        FROM 
             solutions sol, 
             graph graphe
        INNER JOIN (
            SELECT 
                   count(s.id) as cnt, 
                   avg(s.score) as av, 
                   g.id as gid 
            FROM 
                 graph g, 
                 solutions s 
            WHERE 
                  g.id = s.id_graph 
            GROUP BY 
                     g.id
        ) t2
        WHERE 
              graphe.id = t2.gid
        AND (
            t2.cnt < " . $_GET['top_to_keep'] . " 
            OR t2.av > 7000 
            OR " . $_GET['top_to_keep'] . " <= (
                SELECT 
                       count(s.id) as mini 
                FROM 
                     graph g, 
                     solutions s 
                WHERE 
                      g.id = s.id_graph 
                GROUP BY 
                         g.id 
                order by mini asc limit 1
            )
        )
        AND sol.id_graph = graphe.id
        ORDER BY RAND() LIMIT 1
    ";
    $stmt = $bdd->prepare($sql);
    $stmt->execute();
    $result = $stmt->fetch(PDO::FETCH_ASSOC);

    $id = str_replace("\n", "-", $result["id"]);
    $points = str_replace("\n", "-", $result["points"]);
    $hit_points = str_replace("\n", "-", $result["ordered_hit_points"]);

    $response = $id . "/" . $points . "/" . $hit_points;
} else if (isset($_GET['all_graphs_id'])) {
    $response = "";

    $sql = "SELECT id FROM graph";
    foreach  ($bdd->query($sql) as $result) {
        $response .= "ID=" . $result["id"] . " ";
    }
} else if (isset($_GET['get_graph'])
    && isset($_GET['id'])) {

    $stmt = select_a_from_b_where_c_is_d($bdd, "points, hit_points", "graph", "id", $_GET['id']);
    $result = $stmt->fetch(PDO::FETCH_ASSOC);

    $points = str_replace("\n", "-", $result["points"]);
    $hit_points = str_replace("\n", "-", $result["hit_points"]);

    $response = $points . "/" . $hit_points;
} else if (isset($_GET['get_graph_id_from_md5'])
    && isset($_GET['md5'])) {
    // input  : graph md5
    // output : graph id

    // pour qu'on ne puisse pas envoyer plusieurs fois le meme graphe
    $hash = md5($_POST['points'] . $_POST['hit_points']);

    $stmt = $bdd->prepare('SELECT id FROM graph WHERE hash = ?');
    $stmt->bindParam(1, $_GET['md5']);

    if($stmt->execute()){
        $response = $stmt->fetch(PDO::FETCH_ASSOC)["id"];
    } else {
        $response = "NOT_FOUND";
    }
}


// --- --- --- Fonctions utilitaires --- --- --- //

function row_exists_in(PDO $bdd, $field, $val, $table){
    $sql = "SELECT count(id) as nb FROM ".$table." WHERE ".$field."='".$val."'";
    $stmt = $bdd->prepare($sql);
    $stmt->execute();
    //$result = $stmt->get_result();
    $count = (int) $stmt->fetch(PDO::FETCH_ASSOC)["nb"];

    if ($count > 0) return true;
    return false;
}

function select_a_from_b_where_c_is_d(PDO $bdd, $select, $from, $where, $is){
    $sql = "SELECT " . $select . " FROM " . $from . " WHERE " . $where. "='" . $is . "'";
    $stmt = $bdd->prepare($sql);
    $stmt->execute();

    return $stmt; //$stmt->get_result();
}


?>

<? echo $response ?>
