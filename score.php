<?php

try{
    $bdd = new PDO('mysql:host=db5001891077.hosting-data.io;dbname=dbs1550468;charset=utf8', 'dbu1547867', '0n-V4-F4ir3-Mi3ux-Qu-4ndr3');
} catch (Exception $e) {
    die('Erreur : ' . $e->getMessage());
}

$sql = "
SELECT avg(t2.score) as av
FROM graph
INNER JOIN (select s.id_graph as gid ,min(s.score) as score from solutions s group by s.id_graph) t2
on (graph.id = t2.gid)
";

$stmt = $bdd->prepare($sql);
$stmt->execute();
$result = $stmt->fetch(PDO::FETCH_ASSOC);


?>

<html>
    <head>
        <title>Score AAGA</title>
    </head>
    <body style="background-color: #313131;">
        <div style="margin: auto;margin-left: 20%;margin-right: 20%;padding: 50px;background-color: #f5ede3;border-radius: 20px;font-family: monospace;margin-top: 100px">
            <p style="text-align: center;font-size: xxx-large">SCORE : <strong><?php echo $result["av"] ?></strong></p>
        </div>
    </body>
</html>
