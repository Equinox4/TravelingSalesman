package algorithms;

import javafx.util.Pair;
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

    public Pair<ArrayList<Point>, ArrayList<Point>> getOneGraphWithNoSolution(){
        Pair<ArrayList<Point>, ArrayList<Point>> result = null;
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
            Random random_generator = new Random();
            int r_index = random_generator.nextInt(allGraphWithoutSolution.size() - 1);

            result = getGraphFromId(r_index);
        } catch(Exception e) {
        } finally {
            // Si la connexion à la BDD n'est pas possible

            if(result == null){
                ArrayList<Integer> candidateIds = getLocalIdListOfGraphWithoutSolution();
                if(candidateIds.isEmpty()) return null;

                Random random_generator = new Random();
                int r_index = random_generator.nextInt(candidateIds.size() - 1);

                result = getGraphFromId(r_index);
            }
        }

        return result;
    }

    public Pair<ArrayList<Point>, ArrayList<Point>> getOneGraphWithSolution(){
        Pair<ArrayList<Point>, ArrayList<Point>> result = null;
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

            result = getGraphFromId(r_index);
        } catch(Exception e) {
        } finally {
            // Si la connexion à la BDD n'est pas possible

            if(result == null){
                ArrayList<Integer> listOfGraphFilesIds = getListOfFileIdWithNameRegexAndFolderPath(GRAPH_FOLDER, GRAPH_FILE_REGEX);
                if(listOfGraphFilesIds.isEmpty()) return null;

                Random random_generator = new Random();
                int r_index = random_generator.nextInt(listOfGraphFilesIds.size() - 1);

                result = getGraphFromId(r_index);
            }
        }

        return result;
    }

    public ArrayList<Integer> getLocalIdListOfGraphWithoutSolution(){
        ArrayList<Integer> listOfGraphFilesIds = getListOfFileIdWithNameRegexAndFolderPath(GRAPH_FOLDER, GRAPH_FILE_REGEX);
        ArrayList<Integer> listOfSolutionsFilesIds = getListOfFileIdWithNameRegexAndFolderPath(SOLUTIONS_FOLDER, SOLUTION_FILE_REGEX);

        listOfGraphFilesIds.removeAll(listOfSolutionsFilesIds);
        return listOfGraphFilesIds;
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

    // Ajouter support offline
    public Pair<ArrayList<Point>, ArrayList<Point>> getGraphFromId(int id) {
        Pair<ArrayList<Point>, ArrayList<Point>> resultat = null;

        try {
            HashMap<String, String> parameters = new HashMap<>();
            parameters.put("get_graph", "");
            parameters.put("id", "" + id);
            String response = get(SERVEUR, parameters);

            String[] parts = response.split("/");
            String points_str = parts[0];
            String hitPoints_str = parts[1];

            // Lecture de tous les points du graphe
            Pattern pattern = Pattern.compile("([0-9]+?) ([0-9]+?)");
            Matcher matcher = pattern.matcher(points_str);

            ArrayList<Point> points = new ArrayList<>();
            while (matcher.find()) {
                points.add(new Point(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))));
            }

            // Lecture de tous les hitPoints du graphe
            pattern = Pattern.compile("([0-9]+?) ([0-9]+?)");
            matcher = pattern.matcher(hitPoints_str);

            ArrayList<Point> hitPoints = new ArrayList<>();
            while (matcher.find()) {
                hitPoints.add(new Point(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))));
            }

            resultat = new Pair<>(points, hitPoints);
        } catch(Exception e) {
        } finally {
            // Ici on part du principe qu'il y a des solutions en local car sinon on serait plutot en mode CREATE_SOLUTION
            if(resultat == null || resultat.getKey().isEmpty() || resultat.getValue().isEmpty()){
                ArrayList<Point> points = readFromFile(GRAPH_FOLDER + "graph_" + id + ".points");

                List<Integer> listOfSolutionsWithScore = getTopNOfGraphSolutionLocal(id, DefaultTeam.TOP_TO_KEEP);
                Random random_generator = new Random();
                int r_index = random_generator.nextInt(listOfSolutionsWithScore.size() - 1);
                int score = listOfSolutionsWithScore.get(r_index);

                ArrayList<Point> solution = readFromFile(SOLUTIONS_FOLDER + "solution_" + id + "_" + score + ".points");

                resultat = new Pair<>(points, solution);
            }
        }


        return resultat;
    }

    /*
    public List<Pair<Integer, Integer>> getTopNOfAllGraphSolutionLocal(int n){
        File folder = new File(SOLUTIONS_FOLDER);

        File[] listOfFiles = folder.listFiles();
        ArrayList<Pair<Integer, Integer>> listOfSolutionsWithScore = new ArrayList<>(); // Paire <ID graphe, score solution>

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                String fileName = listOfFiles[i].getName();

                Pattern pattern = Pattern.compile(SOLUTION_FILE_REGEX);
                Matcher matcher = pattern.matcher(fileName);

                if(matcher.find()){
                    listOfSolutionsWithScore.add(new Pair(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))));
                }
            }
        }

        Collections.sort(listOfSolutionsWithScore, new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(final Pair<Integer, Integer> o1, final Pair<Integer, Integer> o2) {
                // ordre decroissant
                if (o1.getValue() > o2.getValue()) return -1;
                else if (o1.getValue().equals(o2.getValue())) return 0;
                else return 1;
            }
        });

        return listOfSolutionsWithScore.subList(0, n);
    }
    */

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
}