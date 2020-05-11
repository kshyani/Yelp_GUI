import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DBConnection {
    static long successCount = 0;

    public String query = "";
    public  Connection connection;

    public DBConnection(){
    }
    public void DBConnect() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
        } catch (Exception e) {
            System.err.println("Unable to load driver.");
            e.printStackTrace();
        }
        try {
            connection = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1525:XE", "system", "oracle");
        }catch (Exception e){
            System.err.println("Unable to connect");
            e.printStackTrace();
        }
    }

    public void DBClose(){
        try{
            connection.close();
        }
        catch (Exception e){
            System.err.println("Unable to close db");
            e.printStackTrace();
        }
    }

    public void insertQueriesInDb() throws IOException, SQLException {
        Connection dbConnection = this.connection;
        Statement statement = dbConnection.createStatement();
        String line = "";
        int count = 0;

        long startTime = System.nanoTime();
        FileReader fr = new FileReader(JsonParsing.outputFile);
        BufferedReader brd = new BufferedReader(fr);
        long updatedRows = 0;
        while (line != null) {
            line = brd.readLine();

            System.out.println(line);
            statement.addBatch(line);
            count++;
            System.out.println(count);
            if(count >5000){
                int[] updatedCont = statement.executeBatch();
                long successCount = Arrays.stream(updatedCont).filter(i -> i==1).count();

                updatedRows = updatedRows +successCount;
                System.out.println("Updated Rows : " + updatedRows);
                System.out.println("=============================================");
                count =0;
                statement.close();
                statement = dbConnection.createStatement();
            }
        }
        int[] updatedCont2 = statement.executeBatch();
        long successCount = Arrays.stream(updatedCont2).filter(i -> i==1).count();
        System.out.println("Running last batch");
        updatedRows = updatedRows +successCount;
        System.out.println("=============================================");
        System.out.println("Get count of all SQL statements from .sql file generated from this program");
        System.out.println("Updated Rows : " + updatedRows);
        System.out.println("=============================================");

        statement.close();
        long endTime = System.nanoTime();
        long timeFinal = endTime-startTime;
        System.out.println("This is final Time : " + timeFinal);
        brd.close();
        fr.close();
    }

    public List<String> getAllCategories(){
        List<String> cats = new ArrayList<String>();
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT DISTINCT MAINCATEGORY FROM yelp_MAIN_ctgry  ORDER BY MAINCATEGORY");
            ResultSet rs = statement.executeQuery();
            while (rs.next()){
                System.out.println(rs.getString(1));
                cats.add(rs.getString(1));
            }
        }catch (SQLException se){
            System.err.println("Query error: SQL Exception");
            se.printStackTrace();
        }catch (Exception e){
            System.err.println("Query error ");
            e.printStackTrace();
        }
        cats = Arrays.asList("Active Life","Arts & Entertainment","Automotive","Car Rental","Cafes","Beauty & Spas","Convenience Stores","Dentists","Doctors","Drugstores","Department Stores","Education","Event Planning & Services","Flowers & Gifts","Food","Health & Medical","Home Services","Home & Garden","Hospitals","Hotels & Travel","Hardware Stores","Grocery","Medical Centers","Nurseries & Gardening","Nightlife","Restaurants","Shopping","Transportation");
        return cats;
    }

    public  ArrayList<String> getSubCategories(ArrayList<String> selectedCategories, String condition){
        ArrayList<String> subs = new ArrayList<String>();
        try {
            String query;
            if(condition == "AND"){
                String subq = "(SELECT businessId FROM yelp_biz_main_ctgry WHERE  bus_categories = '"+selectedCategories.get(0)+"')";
                if(selectedCategories.size() > 1){
                    for(int i=1; i<selectedCategories.size(); i++){
                        subq += " INTERSECT (SELECT businessId FROM yelp_biz_main_ctgry WHERE  bus_categories = '"+selectedCategories.get(i) +"' )";
                    }
                }
                //query = "SELECT DISTINCT bs.subcategory FROM BUSINESS_SUBCAT bs JOIN BUSINESS_CAT bc on bs.BUSINESSID = bc.BUSINESSID  WHERE bs.businessId IN ( "+subq+" )";
                query = " select distinct(SUBCATEGORY) from yelp_biz_sub_ctgry where (" +
                        "            BUSINESSID in (" +
                        subq +
                        "))" ;
            }else{
                String subq = "(SELECT businessId FROM yelp_biz_main_ctgry WHERE  bus_categories = '"+selectedCategories.get(0)+"')";
                if(selectedCategories.size() > 1){
                    for(int i=1; i<selectedCategories.size(); i++){
                        subq += " union (SELECT businessId FROM yelp_biz_main_ctgry WHERE  bus_categories = '"+selectedCategories.get(i) +"' )";
                    }
                }
                //query = "SELECT DISTINCT bs.subcategory FROM BUSINESS_SUBCAT bs JOIN BUSINESS_CAT bc on bs.BUSINESSID = bc.BUSINESSID  WHERE bs.businessId IN ( "+subq+" )";
                query = " select distinct(SUBCATEGORY) from yelp_biz_sub_ctgry where (" +
                        "            BUSINESSID in (" +
                        subq +
                        "))" ;
            }
            System.out.println(query);

            PreparedStatement statement = connection.prepareStatement(query);

            ResultSet rs = statement.executeQuery();
            while (rs.next()){
                System.out.println(rs.getString(1));
                //BusinessSubCategories sub = new BusinessSubCategories();
                //sub.setSubCategoryName(rs.getString(1));
                String sub = rs.getString(1);
                subs.add(sub.toString());
            }
        }catch (SQLException se){
            System.err.println("Query error: SQL Exception");
            se.printStackTrace();
        }catch (Exception e){
            System.err.println("Query error ");
            e.printStackTrace();
        }
        return subs;
    }

    public ArrayList<Business> queryBusinessByCategory(ArrayList<String> selectedCategories, String condition){




        ArrayList<Business> businesses = new ArrayList<Business>();
        String query;
        if(condition == "AND"){
            String subq = "\n\t(SELECT businessId FROM yelp_biz_main_ctgry WHERE  BUS_CATEGORIES = '"+selectedCategories.get(0)+"')\t";
            if(selectedCategories.size() > 1){
                for(int i=1; i<selectedCategories.size(); i++){
                    subq += "\tINTERSECT\n\t  (SELECT businessId FROM yelp_biz_main_ctgry WHERE  BUS_CATEGORIES = '"+selectedCategories.get(i) +"' )";
                }
            }
            query = "SELECT DISTINCT businessId, BUSINESSNAME, city, state, stars  FROM yelp_biz WHERE businessId IN ( "+subq+" \n)";

        }else{
            String subq = "\n\t(SELECT businessId FROM yelp_biz_main_ctgry WHERE  BUS_CATEGORIES = '"+selectedCategories.get(0)+"')\t";
            if(selectedCategories.size() > 1){
                for(int i=1; i<selectedCategories.size(); i++){
                    subq += "\tUNION\n\t  (SELECT businessId FROM yelp_biz_main_ctgry WHERE  BUS_CATEGORIES = '"+selectedCategories.get(i) +"' )";
                }
            }
            query = "SELECT DISTINCT businessId, BUSINESSNAME, city, state, stars  FROM yelp_biz WHERE businessId IN ( \n\t"+subq+" \n)";

        }
        System.out.println(query);
        this.query = query;
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet rs = statement.executeQuery();
            while (rs.next()){
                Business b = new Business();
                b.setBusinessId(rs.getString(1));
                b.setName(rs.getString(2));
                b.setCity(rs.getString(3));
                b.setState(rs.getString(4));
                b.setStars(rs.getFloat(5));
                businesses.add(b);
            }
        }catch (SQLException se){
            System.err.println("Query error: SQL Exception");
            se.printStackTrace();
        }catch (Exception e){
            System.err.println("Query error ");
            e.printStackTrace();
        }
        return businesses;
    }

    public ArrayList<String> getAttributes(ArrayList<String> selectedSubCategories, ArrayList<String> selectedCategories, String condition){
        ArrayList<String> attrs = new ArrayList<String>();
        try {
            String query = "";
            if(condition == "AND"){

                String subq = "(SELECT businessId FROM yelp_biz_main_ctgry WHERE  BUS_CATEGORIES = '"+selectedCategories.get(0)+"')";
                if(selectedCategories.size() > 1){
                    for(int i=1; i<selectedCategories.size(); i++){
                        subq += " INTERSECT (SELECT businessId FROM yelp_biz_main_ctgry WHERE  BUS_CATEGORIES = '"+selectedCategories.get(i) +"' )";
                    }
                }
                query = subq;
                System.out.println(subq);
                //query = "SELECT DISTINCT ba.attr_name FROM BUSINESS_ATTR ba JOIN BUSINESS_SUBCAT bs on bs.BUSINESSID = ba.BUSINESSID JOIN BUSINESS_CAT BC on ba.BUSINESSID = BC.BUSINESSID WHERE ba.businessId IN ( "+subq+" )";

            }else {
                String subq = "(SELECT businessId FROM yelp_biz_main_ctgry WHERE  BUS_CATEGORIES = '"+selectedCategories.get(0)+"')";
                if(selectedCategories.size() > 1){
                    for(int i=1; i<selectedCategories.size(); i++){
                        subq += " union (SELECT businessId FROM yelp_biz_main_ctgry WHERE  BUS_CATEGORIES = '"+selectedCategories.get(i) +"' )";
                    }
                }
                query = subq;
                System.out.println(subq);
                //query = "SELECT DISTINCT ba.attr_name FROM BUSINESS_ATTR ba JOIN BUSINESS_SUBCAT bs on bs.BUSINESSID = ba.BUSINESSID JOIN BUSINESS_CAT BC on ba.BUSINESSID = BC.BUSINESSID WHERE ba.businessId IN ( "+subq+" )";


            }

            String subq2 = "select BUSINESSID from yelp_biz_sub_ctgry where (" +
                    " BUSINESSID in (";

            String outerQStart = subq2 + query + ")  AND  (SUBCATEGORY = '";
            String outerQEnd = "'))";

            //query += " AND bs.subcategory = '" + selectedSubCategories.get(0) + "' ";
            String finalQuery = outerQStart + selectedSubCategories.get(0) + outerQEnd;
            if (selectedSubCategories.size() > 1) {
                for (int i = 1; i < selectedSubCategories.size(); i++) {
                    //query += condition + " bs.subcategory = '" + selectedSubCategories.get(i) + "' ";
                    String temp = "INTERSECT " + outerQStart + selectedSubCategories.get(i) + outerQEnd;
                    finalQuery = finalQuery + temp;
                }
            }
            System.out.println(finalQuery);

            if(condition == "OR"){
                finalQuery = finalQuery.replaceAll("INTERSECT","UNION");
            }
            String endQS = "select distinct(ATTRIBUTENAME) from yelp_biz_attributes where BUSINESSID in (";
            String endQE = ")";
            finalQuery = endQS + finalQuery + endQE;
            PreparedStatement statement = connection.prepareStatement(finalQuery);
            ResultSet rs = statement.executeQuery();
            while (rs.next()){
                System.out.println(rs.getString(1));
                //Attribute attribute = new Attribute();
                //attribute.setAttribute_name(rs.getString(1));
                String attribute = rs.getString(1);
                attrs.add(attribute);
            }
        }catch (SQLException se){
            System.err.println("Query error: SQL Exception");
            se.printStackTrace();
        }catch (Exception e){
            System.err.println("Query error ");
            e.printStackTrace();
        }
        return attrs;
    }

    public ArrayList<Business> queryBusinessByCategorySubCategory(ArrayList<String> selectedCategories, ArrayList<String> selectedSubCategories, String condition){

        ArrayList<Business> businesses = new ArrayList<Business>();
        try {
            String query = "";
            if(condition == "AND"){

                String subq = "\n\t\t(SELECT businessId FROM yelp_biz_main_ctgry WHERE  BUS_CATEGORIES = '"+selectedCategories.get(0)+"')";
                if(selectedCategories.size() > 1){
                    for(int i=1; i<selectedCategories.size(); i++){
                        subq += "\t INTERSECT \n\t\t (SELECT businessId FROM yelp_biz_main_ctgry WHERE  BUS_CATEGORIES = '"+selectedCategories.get(i) +"' )";
                    }
                }
                query = subq;
                System.out.println(subq);
                //query = "SELECT DISTINCT ba.attr_name FROM BUSINESS_ATTR ba JOIN BUSINESS_SUBCAT bs on bs.BUSINESSID = ba.BUSINESSID JOIN BUSINESS_CAT BC on ba.BUSINESSID = BC.BUSINESSID WHERE ba.businessId IN ( "+subq+" )";

            }else {

                String subq = "\t\n\t\t (SELECT businessId FROM yelp_biz_main_ctgry WHERE  BUS_CATEGORIES = '"+selectedCategories.get(0)+"')";
                if(selectedCategories.size() > 1){
                    for(int i=1; i<selectedCategories.size(); i++){
                        subq += "\t UNION \n\t\t (SELECT businessId FROM yelp_biz_main_ctgry WHERE  BUS_CATEGORIES = '"+selectedCategories.get(i) +"' )";
                    }
                }
                query = subq;
                System.out.println(subq);
                //query = "SELECT DISTINCT ba.attr_name FROM BUSINESS_ATTR ba JOIN BUSINESS_SUBCAT bs on bs.BUSINESSID = ba.BUSINESSID JOIN BUSINESS_CAT BC on ba.BUSINESSID = BC.BUSINESSID WHERE ba.businessId IN ( "+subq+" )";


            }

            String subq2 = "\n\tselect BUSINESSID from yelp_biz_sub_ctgry where (" +
                    " BUSINESSID in (";

            String outerQStart = subq2 + query + "\n\t)  AND  (SUBCATEGORY = '";
            String outerQEnd = "'))";

            //query += " AND bs.subcategory = '" + selectedSubCategories.get(0) + "' ";
            String finalQuery = outerQStart + selectedSubCategories.get(0) + outerQEnd;
            if (selectedSubCategories.size() > 1) {
                for (int i = 1; i < selectedSubCategories.size(); i++) {
                    //query += condition + " bs.subcategory = '" + selectedSubCategories.get(i) + "' ";
                    if(condition == "AND"){
                        String temp = "\t INTERSECT \n\t\t" + outerQStart + selectedSubCategories.get(i) + outerQEnd;
                        finalQuery = finalQuery + temp;
                    }else{
                        String temp = "" + outerQStart + selectedSubCategories.get(i) + outerQEnd;
                        finalQuery = finalQuery + "\t UNION \n\t\t" +temp;
                    }

                }
            }
            System.out.println(finalQuery);

            String endQS = "SELECT businessId, BUSINESSNAME, city, state, stars  FROM yelp_biz WHERE businessId IN (\n";
            String endQE = "\n)";
            finalQuery = endQS + finalQuery + endQE;
            this.query = finalQuery;
            PreparedStatement statement = connection.prepareStatement(finalQuery);
            ResultSet rs = statement.executeQuery();
            while (rs.next()){
                System.out.println(rs.getString(1));
                //Attribute attribute = new Attribute();
                //attribute.setAttribute_name(rs.getString(1));
                Business b = new Business();
                b.setBusinessId(rs.getString(1));
                b.setName(rs.getString(2));
                b.setCity(rs.getString(3));
                b.setState(rs.getString(4));
                b.setStars(rs.getFloat(5));
                businesses.add(b);
            }
        }catch (SQLException se){
            System.err.println("Query error: SQL Exception");
            se.printStackTrace();
        }catch (Exception e){
            System.err.println("Query error ");
            e.printStackTrace();
        }
        return businesses;
    }

    public ArrayList<Business> queryBusinessByCategorySubCategoryAttributes(ArrayList<String> selectedCategories, ArrayList<String> selectedSubCategories,ArrayList<String> selectedAttributes, String condition) {

        ArrayList<Business> businesses = new ArrayList<Business>();
        try {
            String query = "";
            if(condition == "AND"){

                String subq = "\n\t\t\t\t(SELECT businessId FROM yelp_biz_main_ctgry WHERE  BUS_CATEGORIES = '"+selectedCategories.get(0)+"')";
                if(selectedCategories.size() > 1){
                    for(int i=1; i<selectedCategories.size(); i++){
                        subq += " \tINTERSECT\n\t\t\t\t (SELECT businessId FROM yelp_biz_main_ctgry WHERE  BUS_CATEGORIES = '"+selectedCategories.get(i) +"' )";
                    }
                }
                query = subq;
                System.out.println(subq);
                //query = "SELECT DISTINCT ba.attr_name FROM BUSINESS_ATTR ba JOIN BUSINESS_SUBCAT bs on bs.BUSINESSID = ba.BUSINESSID JOIN BUSINESS_CAT BC on ba.BUSINESSID = BC.BUSINESSID WHERE ba.businessId IN ( "+subq+" )";

            }else {

                String subq = "\n\t\t\t\t(SELECT businessId FROM yelp_biz_main_ctgry WHERE  BUS_CATEGORIES = '"+selectedCategories.get(0)+"')";
                if(selectedCategories.size() > 1){
                    for(int i=1; i<selectedCategories.size(); i++){
                        subq += "\tUNION\n\t\t\t\t (SELECT businessId FROM yelp_biz_main_ctgry WHERE  BUS_CATEGORIES = '"+selectedCategories.get(i) +"' )";
                    }
                }
                query = subq;
                System.out.println(subq);
                //query = "SELECT DISTINCT ba.attr_name FROM BUSINESS_ATTR ba JOIN BUSINESS_SUBCAT bs on bs.BUSINESSID = ba.BUSINESSID JOIN BUSINESS_CAT BC on ba.BUSINESSID = BC.BUSINESSID WHERE ba.businessId IN ( "+subq+" )";


            }

            String subq2 = "\n\t\t\t\tselect BUSINESSID from yelp_biz_sub_ctgry where (" +
                    " BUSINESSID in (";

            String outerQStart = subq2 + query + "\n\t\t\t\t)  AND  (SUBCATEGORY = '";
            String outerQEnd = "'))";

            //query += " AND bs.subcategory = '" + selectedSubCategories.get(0) + "' ";
            String finalQuery = outerQStart + selectedSubCategories.get(0) + outerQEnd;
            if (selectedSubCategories.size() > 1) {
                for (int i = 1; i < selectedSubCategories.size(); i++) {
                    //query += condition + " bs.subcategory = '" + selectedSubCategories.get(i) + "' ";
                    String temp = "\tINTERSECT\n\t\t\t\t " + outerQStart + selectedSubCategories.get(i) + outerQEnd;
                    finalQuery = finalQuery + temp;
                }
            }
            System.out.println(finalQuery);


            String startPart = "\tselect businessid from yelp_biz_attributes where " +"Businessid in (";
            String endPart = "\n\t) AND ATTRIBUTENAME = ";
            String attribute0 = selectedAttributes.get(0);
            String finalCatSubCatAttributeQuery = "";
            finalCatSubCatAttributeQuery = startPart + finalQuery + endPart + "'" + attribute0 + "'";
            if(selectedAttributes.size() > 1){
                for(int i=1;i<selectedAttributes.size();i++){
                    String currentAttrQuery = "\tINTERSECT\n" + startPart + finalQuery + endPart  + "'" + selectedAttributes.get(i) + "'";
                    finalCatSubCatAttributeQuery = finalCatSubCatAttributeQuery + currentAttrQuery;
                }
            }
            System.out.println(finalCatSubCatAttributeQuery);
            if(condition == "OR"){
                finalCatSubCatAttributeQuery = finalCatSubCatAttributeQuery.replaceAll("INTERSECT","UNION");
            }


            String finalQuery2 = "";
            String endQS = "SELECT DISTINCT businessId, BUSINESSNAME, city, state, stars  FROM yelp_biz WHERE businessId IN (\n";
            String endQE = "\n)";
            finalQuery2 = endQS + finalCatSubCatAttributeQuery + endQE;

            if(condition == "OR"){
                finalQuery2 = finalQuery2.replaceAll("INTERSECT","UNION");
            }

            this.query = finalQuery2;
            PreparedStatement statement = connection.prepareStatement(finalQuery2);
            ResultSet rs = statement.executeQuery();
            while (rs.next()){
                System.out.println(rs.getString(1));
                //Attribute attribute = new Attribute();
                //attribute.setAttribute_name(rs.getString(1));
                Business b = new Business();
                b.setBusinessId(rs.getString(1));
                b.setName(rs.getString(2));
                b.setCity(rs.getString(3));
                b.setState(rs.getString(4));
                b.setStars(rs.getFloat(5));
                businesses.add(b);
            }
        }catch (SQLException se){
            System.err.println("Query error: SQL Exception");
            se.printStackTrace();
        }catch (Exception e){
            System.err.println("Query error ");
            e.printStackTrace();
        }
        return businesses;

    }

    public ArrayList<Reviews> getReviews(String businessId) {
        return null;
    }
}
