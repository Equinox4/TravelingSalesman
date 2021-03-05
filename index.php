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
    $stmt = $bdd->prepare('SELECT ordered_hit_points, score FROM solutions WHERE id_graph = ? ORDER BY ASC score LIMIT ?');
    $stmt->bindParam(1, $_POST['id_graph']);
    $stmt->bindParam(2, $_POST['n']);

    if($stmt->execute()){
        foreach ($stmt->fetchAll(PDO::FETCH_ASSOC) as $result) {
            $response .= "\nSCORE=" . $result["score"] . "\n" . $result["ordered_hit_points"];
        }
    } else {
        $response = "GRAPH_SOLUTION_DOESNT_EXISTS";
    }
} else if (isset($_GET['solutions'])
    && isset($_GET['best_of_hash'])) {
    $response = "";

    // on recherche le meilleur pour ce graphe
    $stmt = $bdd->prepare('SELECT s.ordered_hit_points as points FROM graph g, solutions s WHERE g.hash = ? AND g.id = s.id_graph ORDER BY s.score ASC LIMIT 1');
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
