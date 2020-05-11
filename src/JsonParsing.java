import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.*;


public class JsonParsing {

    static File outputFile;
    static FileWriter fileWriter;

    static {
        outputFile = new File("output_03_06.sql");
        if (outputFile.exists())
            outputFile.delete();
        try {
            fileWriter = new FileWriter(outputFile, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static File yelpBusinessFile;
    static File yelpCheckinFile;
    static File yelpReviewFile;
    static File yelpUserFile;

    /*static LineIterator yelpBusinessFileIterator ;
    static LineIterator yelpCheckinFileIterator ;
    static LineIterator yelpReviewFileIterator ;
    static LineIterator yelpUserFileIterator ;*/

    static HashSet<String> mainCategories = new HashSet<String>(Arrays.asList("Active Life", "Arts & Entertainment", "Automotive", "Car Rental", "Cafes", "Beauty & Spas", "Convenience Stores", "Dentists", "Doctors", "Drugstores", "Department Stores", "Education", "Event Planning & Services", "Flowers & Gifts", "Food", "Health & Medical", "Home Services", "Home & Garden", "Hospitals", "Hotels & Travel", "Hardware Stores", "Grocery", "Medical Centers", "Nurseries & Gardening", "Nightlife", "Restaurants", "Shopping", "Transportation"));

    public JsonParsing() throws IOException {
    }


    public void createFile(String fileName) throws IOException {
        if (fileName.equals("yelp_business.json")) {
            yelpBusinessFile = new File(
                    getClass().getClassLoader().getResource("yelp_business.json").getFile()
            );
            //yelpBusinessFile = new File(fileName);
            //yelpBusinessFileIterator = FileUtils.lineIterator(yelpBusinessFile, "UTF-8");
        } else if (fileName.equals("yelp_user.json")) {
            //yelpUserFile = new File(fileName);
            yelpUserFile = new File(
                    getClass().getClassLoader().getResource("yelp_user.json").getFile()
            );
            //yelpUserFileIterator = FileUtils.lineIterator(yelpUserFile, "UTF-8");
        } else if (fileName.equals("yelp_review.json")) {
            //yelpReviewFile = new File(fileName);
            yelpReviewFile = new File(
                    getClass().getClassLoader().getResource("yelp_review.json").getFile()
            );
            //yelpReviewFileIterator = FileUtils.lineIterator(yelpReviewFile, "UTF-8");
        }

    }

    public static void parseBusinessFile() throws IOException {
        FileReader fr = new FileReader(yelpBusinessFile);
        BufferedReader brd = new BufferedReader(fr);
        String line = brd.readLine();
        while (line != null) {
            JsonParsing.parseBusinessObject(line);
            line = brd.readLine();
        }
        brd.close();
        fr.close();
    }

    public static void parseUserFile() throws IOException {
        FileReader fr = new FileReader(yelpUserFile);
        BufferedReader brd = new BufferedReader(fr);
        String line = brd.readLine();
        while (line != null) {
            JsonParsing.parseUserObject(line);
            line = brd.readLine();
        }
        brd.close();
        fr.close();
    }

    public static void parseReviewFile() throws IOException {
        FileReader fr = new FileReader(yelpReviewFile);
        BufferedReader brd = new BufferedReader(fr);
        String line = brd.readLine();
        while (line != null) {
            JsonParsing.parseReviewObject(line);
            line = brd.readLine();
        }
        brd.close();
        fr.close();
    }

    public static void parseBusinessObject(String businessObjectString) {
        JSONObject jsonBusinessObject = new JSONObject(businessObjectString);
        String businessId = (String) jsonBusinessObject.get("business_id");
        String businessName = (String) jsonBusinessObject.get("name");
        String city = (String) jsonBusinessObject.get("city");
        String state = (String) jsonBusinessObject.get("state");
        int reviewCount = (int) jsonBusinessObject.getInt("review_count");
        double stars = jsonBusinessObject.getDouble("stars");

        /*JSONArray jsonCategories = jsonBusinessObject.getJSONArray("categories");

        StringJoiner finalMainCatString = new StringJoiner(",");
        StringJoiner finalSubCatString = new StringJoiner(",");

        for (int i = 0; i < jsonCategories.length(); i++) {
            String tempCategory = (String) jsonCategories.get(i);
            if(mainCategories.contains(tempCategory)){
                finalMainCatString.add(tempCategory);
            }else{
                finalSubCatString.add(tempCategory);
            }
        }*/

        ArrayList<String> finalMainCatList = new ArrayList<String>();
        ArrayList<String> finalSubCatList = new ArrayList<String>();

        JSONArray jsonCategories = jsonBusinessObject.getJSONArray("categories");
        for (int i = 0; i < jsonCategories.length(); i++) {
            String tempCategory = (String) jsonCategories.get(i);
            if(mainCategories.contains(tempCategory)){
                finalMainCatList.add(tempCategory);
            }else{
                finalSubCatList.add(tempCategory);
            }
        }

        String insertStringForBusinessTable = "INSERT INTO yelp_biz VALUES (" +
                "q'[" + businessId + "]'" + "," +
                "q'[" + businessName + "]'" + "," +
                "q'[" + city + "]'" + "," +
                "q'[" + state + "]'" + "," +
                "" + reviewCount + "" + "," +
                "" + stars  + "" + /*"," +*/
                //"q'[" + finalMainCatString.toString() + "]'" +
                ")";
        try {
            write((String) insertStringForBusinessTable, fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }


        if(finalMainCatList.size() > 0){
            String insertStringForMainCatTable = "INSERT INTO yelp_biz_main_ctgry VALUES (q'[" + businessId + "]',q'[";

            String temp = "";
            for(String tempCat : finalMainCatList){
                temp = insertStringForMainCatTable + tempCat + "]')";
                try {
                    write((String) temp, fileWriter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                temp = null;
            }
        }

        if(finalSubCatList.size() > 0){
            String insertStringForSubCatTable = "INSERT INTO yelp_biz_sub_ctgry VALUES (q'[" + businessId + "]',q'[";

            String temp = "";
            for(String tempSubCat : finalSubCatList){
                temp = insertStringForSubCatTable + tempSubCat + "]')";
                try {
                    write((String) temp, fileWriter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                temp = null;
            }
        }

        List<String> finalListOfAttributesToInsert = new ArrayList<String>();
        JSONObject attributes = jsonBusinessObject.getJSONObject("attributes");
        Iterator<String> attributeKeys = attributes.keys();
        StringBuilder attributeSingleKey;

        while (attributeKeys.hasNext()) {
            ArrayList<String> innerStrs = new ArrayList<String>();
            String currentKey = attributeKeys.next();
            String currentTempKey = new String(currentKey);
            attributeSingleKey = new StringBuilder(currentTempKey.replaceAll(" ", ""));
            if (attributes.get(currentKey) instanceof Boolean) {
                attributeSingleKey.append("_").append(attributes.getBoolean(currentKey));
            } else if (attributes.get(currentKey) instanceof String) {
                attributeSingleKey.append("_").append(attributes.getString(currentKey).replaceAll(" ", ""));
            } else if (attributes.get(currentKey) instanceof JSONObject) {
                JSONObject tempObj = (JSONObject) attributes.get(currentKey);
                Iterator<String> allInnerKeys = (tempObj).keys();
                currentTempKey.replaceAll(" ", "");
                while (allInnerKeys.hasNext()) {
                    String tempKey = allInnerKeys.next();
                    String currentTempKey2 = new String(tempKey);
                    attributeSingleKey.append("_").append(currentTempKey2).append("_").append(((JSONObject) tempObj).get(tempKey));
                    //
                    /*if (tempObj.get(tempKey) instanceof Boolean) {
                        attributeSingleKey.append("_").append(currentTempKey2).append("_").append(tempObj.getBoolean(tempKey));
                    } else if (tempObj.get(tempKey) instanceof String) {
                        attributeSingleKey.append("_").append(currentTempKey2).append("_").append(tempObj.getString(tempKey).replaceAll(" ", ""));
                    } else if (tempObj.get(tempKey) instanceof Integer) {
                        attributeSingleKey.append("_").append(currentTempKey2).append("_").append(tempObj.getInt(tempKey));
                    }else if (tempObj.get(tempKey) instanceof Double) {
                        attributeSingleKey.append("_").append(currentTempKey2).append("_").append(tempObj.getDouble(tempKey));
                    }*/
                    innerStrs.add(attributeSingleKey.toString());
                    attributeSingleKey = new StringBuilder(currentTempKey.replaceAll(" ", ""));
                }
            } else if (attributes.get(currentKey) instanceof Integer) {
                attributeSingleKey.append("_").append(attributes.getInt(currentKey));
            } else {
                System.out.println("ERROR====" + businessId + " " + attributes.get(currentKey).toString());
            }
            if (innerStrs.size() != 0) {
                finalListOfAttributesToInsert.addAll(innerStrs);
                innerStrs = null;
            } else {
                finalListOfAttributesToInsert.add(attributeSingleKey.toString());
            }
            attributeSingleKey = null;
        }
        if(finalListOfAttributesToInsert.size() > 0){
            String insertStringForBusinessAttrsTable = "INSERT INTO yelp_biz_attributes VALUES (q'[" + businessId + "]',q'[";

            String temp = "";
            for(String tempAttr : finalListOfAttributesToInsert){
                temp = insertStringForBusinessAttrsTable + tempAttr + "]')";
                try {
                    write((String) temp, fileWriter);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                temp = null;
            }
        }
    }

    public static void parseReviewObject(String reviewObject) {
        JSONObject jsonReviewObject = new JSONObject(reviewObject);
        String reviewId = (String) jsonReviewObject.get("review_id");
        String userId = (String) jsonReviewObject.get("user_id");
        String businessId = (String) jsonReviewObject.get("business_id");
        String datePosted = (String) jsonReviewObject.get("date");
        double starsCount = jsonReviewObject.getDouble("stars");
        int finalVotesCount = 0;
        JSONObject votesObject = (JSONObject) jsonReviewObject.get("votes");
        for (String key : votesObject.keySet()) {
            int val =  votesObject.getInt(key);
            finalVotesCount += val;
        }
        if(reviewId.equals("3f4iWELs16wiH7167-nCJg")){
            System.out.println("There");
        }
        String insertStringForReviewTable = "INSERT INTO yelp_review VALUES (" +
                "q'[" + reviewId + "]'" + "," +
                "q'[" + userId + "]'" + "," +
                "q'[" + businessId + "]'" + "," +
                "to_date('" + datePosted + "','YYYY-MM-DD')" + "," +
                "" + finalVotesCount + "" + "," +
                "" + starsCount +
                ")";

        try {
            write((String) insertStringForReviewTable, fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void parseUserObject(String userObjectString) throws IOException {
        JSONObject jsonUserObject = new JSONObject(userObjectString);
        String userId = (String) jsonUserObject.get("user_id");
        String userName = (String) jsonUserObject.get("name");
        String yelpingSince = (String) jsonUserObject.get("yelping_since");
        int reviewCount = (int) jsonUserObject.getInt("review_count");
        int friendsCount = jsonUserObject.getJSONArray("friends").length();
        double avgStars = jsonUserObject.getDouble("average_stars");

        int finalVotesCount = 0;
        JSONObject votesObject = (JSONObject) jsonUserObject.get("votes");
        for (String key : votesObject.keySet()) {
            int val =  votesObject.getInt(key);
            finalVotesCount += val;
        }

        String insertStringForUserTable = "INSERT INTO yelp_user VALUES (" +
                "q'[" + userId + "]'" + "," +
                "q'[" + userName + "]'" + "," +
                "to_date('" + yelpingSince + "','RR-MM')" + "," +
                "" + reviewCount + "" + "," +
                "" + friendsCount + "" + "," +
                "" + avgStars + "" + "," +
                "" + finalVotesCount + "" +
                ")";
        write(insertStringForUserTable,fileWriter);

    }

    private static void write(String record, Writer writer) throws IOException {
        writer.write(record);
        writer.write(System.getProperty("line.separator"));
        writer.flush();
    }
}

