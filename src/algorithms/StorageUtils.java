package algorithms;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.HttpClients;

import javax.net.ssl.HttpsURLConnection;
import java.awt.Point;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class StorageUtils {
    private static final String SOLUTION_FILE_REGEX = "solution_([0-9]+?)_([0-9]+?).points";
    private static final String GRAPH_FILE_REGEX = "graph_([0-9]+?).points";
    private static final int FAKE_ID = -1;//435;
    private static String SERVEUR = "https://voycom.desfichesdescartes.fr/index.php";
    private static String GRAPH_FOLDER = "tests/graphs/";
    private static String SOLUTIONS_FOLDER = "tests/solutions/";

    private HttpClient httpclient;
    private HttpPost httppost;

    public StorageUtils(){
        this.httpclient = HttpClients.createDefault();
        this.httppost = new HttpPost(SERVEUR);
    }

    // le edgeThreshold est une constante à 140 dans le GUI donc il n'y a pas besoin de le sauvegarder
    public boolean saveGraph(ArrayList<Point> points, ArrayList<Point> hitPoints) throws Exception {
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("new_graph", "");
        parameters.put("points", pointsToString(points));
        parameters.put("hit_points", pointsToString(hitPoints));
        String response = post(SERVEUR, parameters);

        if(response == "ALREADY_PRESENT") new Exception("This graph is already present in the DB");
        else if(!isNumeric(response)) new Exception("Incorrect response from the server : " + response);
        int graph_id = Integer.parseInt(response); // juste pour la clareté


        saveToFile(GRAPH_FOLDER + "graph_" + graph_id, points);

        return true;
    }

    public boolean saveSolution(int graph_id, ArrayList<Point> best_result, int score) throws Exception {

        // On sauvegarde en local avant pour etre sur de rien perdre si le reseau bug
        saveToFile(SOLUTIONS_FOLDER + "solution_" + graph_id + "_" + score, best_result);

        // sauvegarde en ligne
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("new_solution", "");
        parameters.put("id_graph", "" + graph_id);
        parameters.put("ordered_hit_points", pointsToString(best_result));
        parameters.put("score", "" + score);
        String response = post(SERVEUR, parameters);

        if(response == "ALREADY_PRESENT") new Exception("This exact solution is already present in the DB");
        else if(!isNumeric(response)) new Exception("Incorrect response from the server : " + response);

        return true;
    }

    public boolean deleteSolution(int graph_id, int score){
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("delete_solution", "");
        parameters.put("id_graph", "" + graph_id);
        parameters.put("score", "" + score);
        String response = get(SERVEUR, parameters);

        if(response == "SOLUTION_DOESNT_EXISTS") new Exception("This solution is not present in the DB");
        else if(!isNumeric(response)) new Exception("Incorrect response from the server : " + response);

        return true;
    }

    public ArrayList<Point> getBestSolution(ArrayList<Point> points, ArrayList<Point> hitPoints) {
        String md5 = md5(pointsToString(points) + pointsToString(hitPoints));

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("solutions", "");
        parameters.put("best_of_hash", md5);
        String response = get(SERVEUR, parameters);


        return servPointsParser(response);
    }

    public Graph getGraphToImprove(int topToKeep){
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("get_graph_to_improve", "");
        parameters.put("top_to_keep", "" + topToKeep);
        String response = get(SERVEUR, parameters);

        String[] parts = response.split("/");
        int id = Integer.parseInt(parts[0]);
        String points_str = parts[1];
        String solution_str = parts[2];

        // Lecture de tous les points du graphe
        ArrayList<Point> points = servPointsParser(points_str);

        // Lecture de tous les points de la solution au graphe
        ArrayList<Point> solution = servPointsParser(solution_str);

        return new Graph(id, points, solution);
    }


    public Graph getOneGraphWithNoSolution(){
        Graph result = null;
        try {
            // Si la connexion à la BDD est possible
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("missing_solutions", "");
            String response = get(SERVEUR, parameters);

            Pattern pattern = Pattern.compile("ID=([0-9]+?) ");
            Matcher matcher = pattern.matcher(response);

            ArrayList<Integer> allGraphWithoutSolution = new ArrayList<>();
            while (matcher.find()) {
                allGraphWithoutSolution.add(Integer.parseInt(matcher.group(1)));
            }

            if(allGraphWithoutSolution.size() == 1) return getGraphFromId(allGraphWithoutSolution.get(0));
            if(allGraphWithoutSolution.size() < 1) return null;

            Random random_generator = new Random();
            int r_index = random_generator.nextInt(allGraphWithoutSolution.size() - 1);

            result = getGraphFromId(allGraphWithoutSolution.get(r_index));
        } catch(Exception e) {
        } 
        // Si la connexion à la BDD n'est pas possible
        if(result == null){
            ArrayList<Integer> candidateIds = getLocalIdListOfGraphWithoutSolution();
            if(candidateIds.isEmpty()) return null;

            Random random_generator = new Random();
            int r_index = random_generator.nextInt(candidateIds.size() - 1);

            result = getGraphFromId(candidateIds.get(r_index));
        }
        

        return result;
    }

    public Graph getOneRandomGraphWithSolution(){
        Graph result = null;
        try {
            // Si la connexion à la BDD est possible
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("all_graphs_id", "");
            String response = get(SERVEUR, parameters);

            Pattern pattern = Pattern.compile("ID=([0-9]+?) ");
            Matcher matcher = pattern.matcher(response);

            ArrayList<Integer> allGraphWithoutSolution = new ArrayList<>();
            while (matcher.find()) {
                allGraphWithoutSolution.add(Integer.parseInt(matcher.group(1)));
            }
            Random random_generator = new Random();
            int r_index = random_generator.nextInt(allGraphWithoutSolution.size() - 1);

            result = getGraphFromId(allGraphWithoutSolution.get(r_index));
        } catch(Exception e) {
        } 
        // Si la connexion à la BDD n'est pas possible
        if(result == null){
            ArrayList<Integer> listOfGraphFilesIds = getListOfFileIdWithNameRegexAndFolderPath(GRAPH_FOLDER, GRAPH_FILE_REGEX);
            if(listOfGraphFilesIds.isEmpty()) return null;

            Random random_generator = new Random();
            int r_index = random_generator.nextInt(listOfGraphFilesIds.size() - 1);

            result = getGraphFromId(listOfGraphFilesIds.get(r_index));
        }
        

        return result;
    }

    public ArrayList<Integer> getLocalIdListOfGraphWithoutSolution(){
        ArrayList<Integer> listOfGraphFilesIds = getListOfFileIdWithNameRegexAndFolderPath(GRAPH_FOLDER, GRAPH_FILE_REGEX);
        ArrayList<Integer> listOfSolutionsFilesIds = getListOfFileIdWithNameRegexAndFolderPath(SOLUTIONS_FOLDER, SOLUTION_FILE_REGEX);

        listOfGraphFilesIds.removeAll(listOfSolutionsFilesIds);
        return listOfGraphFilesIds;
    }

    public ArrayList<ArrayList<Point>> getTopNOfGraphSolution(int graphId, int n){
        //getTopNOfGraphSolutionLocal();
        ArrayList<ArrayList<Point>> result = new ArrayList<>();

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("solutions", "");
        parameters.put("n_best_of_one", "");
        parameters.put("id_graph", "" + graphId);
        parameters.put("n", "" + n);
        String response = get(SERVEUR, parameters);

        String[] parts = response.split("/");
        for(int i = 0; i < (response.chars().filter(c -> c == '/').count() + 1); i++){
            String points_str = parts[i];
            result.add(servPointsParser(points_str));
        }

        if(result.isEmpty() && false){ // si on a rien reussi à reccuperer via le réseau
            List<Integer> liste_scores = getTopNOfGraphSolutionLocal(graphId, n);
            for(Integer score : liste_scores){
                result.add(readFromFile(SOLUTIONS_FOLDER + "solution_" + graphId + "_" + score + ".points"));
            }
        }

        return result;
    }

    public ArrayList<Integer> getListOfFileIdWithNameRegexAndFolderPath(String folderPath, String regex){
        File folder = new File(folderPath);

        File[] listOfFiles = folder.listFiles();
        ArrayList<Integer> listOfFilesIds = new ArrayList<>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String fileName = listOfFiles[i].getName();

                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(fileName);

                if(matcher.find()){
                    listOfFilesIds.add(Integer.parseInt(matcher.group(1)));
                }
            }
        }

        return listOfFilesIds;
    }

    // Support partiel de l'offline
    public Graph getGraphFromId(int id) {
        if(FAKE_ID != -1) id = FAKE_ID;
        Graph resultat = null;

        try {
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("get_graph", "");
            parameters.put("id", "" + id);
            String response = get(SERVEUR, parameters);


            String[] parts = response.split("/");
            String points_str = parts[0];
            String hitPoints_str = parts[1];


            // Lecture de tous les points du graphe
            ArrayList<Point> points = servPointsParser(points_str);

            // Lecture de tous les hitPoints du graphe
            ArrayList<Point> hitPoints = servPointsParser(hitPoints_str);

            resultat = new Graph(id, points, hitPoints);
        } catch(Exception e) {
        } 
        // Ici on part du principe que s'il n'est pas connecté il l'a déjà ete au moins une fois au moment de run des tests
        if(resultat == null || resultat.points.isEmpty() || resultat.hitPoints.isEmpty()){
            ArrayList<Point> points = readFromFile(GRAPH_FOLDER + "graph_" + id + ".points");

            List<Integer> listOfSolutionsWithScore = getTopNOfGraphSolutionLocal(id, DefaultTeam.TOP_TO_KEEP);
            if(listOfSolutionsWithScore.size() == 0) return null; // il faut que l'user se connecte
            Random random_generator = new Random();
            int r_index = random_generator.nextInt(listOfSolutionsWithScore.size() - 1);
            int score = listOfSolutionsWithScore.get(r_index);

            ArrayList<Point> solution = readFromFile(SOLUTIONS_FOLDER + "solution_" + id + "_" + score + ".points");

            resultat = new Graph(id, points, solution);
        }
        


        return resultat;
    }

    public int getIdFromGraph(ArrayList<Point> points, ArrayList<Point> hitPoints) {
        String md5 = md5(pointsToString(points) + pointsToString(hitPoints));

        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("get_graph_id_from_md5", "");
        parameters.put("md5", "" + md5);
        String response = get(SERVEUR, parameters);

        if(response == "NOT_FOUND") new Exception("Ths graph haven't been found in the DB");
        else if(!isNumeric(response)) new Exception("Incorrect response from the server : " + response);

        return Integer.parseInt(response);
    }

    public ArrayList<Point> servPointsParser(String points_str){
        Pattern pattern = Pattern.compile("([0-9]+?) ([0-9]+?)-");
        Matcher matcher = pattern.matcher(points_str);

        ArrayList<Point> points = new ArrayList<>();
        while (matcher.find()) {
            points.add(new Point(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))));
        }

        return points;
    }

    public List<Integer> getTopNOfGraphSolutionLocal(int graphId, int n){
        File folder = new File(SOLUTIONS_FOLDER);

        File[] listOfFiles = folder.listFiles();
        ArrayList<Integer> listOfSolutionsScore = new ArrayList<>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String fileName = listOfFiles[i].getName();

                Pattern pattern = Pattern.compile(SOLUTION_FILE_REGEX);
                Matcher matcher = pattern.matcher(fileName);

                if(matcher.find()){
                    if(Integer.parseInt(matcher.group(1)) == graphId){
                        listOfSolutionsScore.add(Integer.parseInt(matcher.group(2)));
                    }
                }
            }
        }

        Collections.sort(listOfSolutionsScore, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                // ordre decroissant
                return -o1.compareTo(o2);
            }
        });

        return listOfSolutionsScore.subList(0, n);
    }

    public String post(String requestURL, HashMap<String, String> postDataParams) {
        URL url;
        String response = "";
        try {
            url = new URL(requestURL);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();
            int responseCode=conn.getResponseCode();

            System.out.println("\nSending 'POST' request to URL : " + requestURL);
            System.out.println("Response Code : " + responseCode);

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    public String get(String requestURL, HashMap<String, String> postDataParams){
        requestURL += "?";
        Iterator it = postDataParams.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            requestURL += pair.getKey().toString() + "=" + pair.getValue().toString() + "&";
            it.remove(); // avoids a ConcurrentModificationException
        }

        URL url;
        String response = "";
        try {
            url = new URL(requestURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");


            int responseCode = con.getResponseCode();
            System.out.println("\nSending 'GET' request to URL : " + requestURL);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer sb = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();

            response = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;

    }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    private String pointsToString(ArrayList<Point> points){
        String result = "";
        for (Point p:points) {
            result += Integer.toString((int)p.getX())+" "+Integer.toString((int)p.getY()) + "\n";
        }
        return result;
    }

    public static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    //FILE PRINTER
    public void saveToFile(String filename,ArrayList<Point> result){
        try {
            while(true){
                BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(filename+".points")));
                try {
                    input.close();
                    System.out.println("Une solution avec le meme score est deja presente dans les fichiers pour ce graphe");
                    return;
                } catch (IOException e) {
                    System.err.println("I/O exception: unable to close "+filename+".points");
                }
            }
        } catch (FileNotFoundException e) {
            printToFile(filename+".points",result);
        }
    }
    private void printToFile(String filename,ArrayList<Point> points){
        try {
            PrintStream output = new PrintStream(new FileOutputStream(filename));
            int x,y;
            for (Point p:points) output.println(Integer.toString((int)p.getX())+" "+Integer.toString((int)p.getY()));
            output.close();
        } catch (FileNotFoundException e) {
            System.err.println("I/O exception: unable to create "+filename);
        }
    }

    //FILE LOADER
    public ArrayList<Point> readFromFile(String filename) {
        String line;
        String[] coordinates;
        ArrayList<Point> points=new ArrayList<Point>();
        try {
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(new FileInputStream(filename))
            );
            try {
                while ((line=input.readLine())!=null) {
                    coordinates=line.split("\\s+");
                    points.add(new Point(Integer.parseInt(coordinates[0]),
                            Integer.parseInt(coordinates[1])));
                }
            } catch (IOException e) {
                System.err.println("Exception: interrupted I/O.");
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    System.err.println("I/O exception: unable to close "+filename);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Input file not found.");
        }
        return points;
    }

    public String md5(String str) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(str.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
        }
        return null;
    }



}