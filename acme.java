import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class acme {

    public static Map<Integer, Double> productsMap = new HashMap<Integer, Double>(); //Cost per product
    public static Map<Integer, Double> ordersTotalMap = new HashMap<Integer, Double>(); //Cost per order
    public static Map<Integer, ArrayList<Integer>> productCustomersMap = new HashMap<>(); //Customers per product
    public static Map<Integer, Double> customerOrdersMap = new HashMap<>(); //Customer's orders
    public static Map<Integer, ArrayList<String>> customersRankingMap = new TreeMap<>(); //Customer's data
    public static List<Map.Entry<Integer, ArrayList<String>>> customersRankingOrderedMap; //Ordered customer's data

    public static void main(String[] args) throws IOException {

        //Load the data
        loadProducts();
        loadOrders();
        loadCustomers();

        //Save the data to respective CSV files
        saveOrderPrices();
        saveOrderCustomers();
        saveCustomerRanking();
    }

    public static void saveOrderPrices() throws IOException {
    
        FileWriter csvWriter = new FileWriter("./interview/order_prices.csv");
        csvWriter.append("id");
        csvWriter.append(",");
        csvWriter.append("euros");
        csvWriter.append("\n");

        for (Map.Entry<Integer, Double> entry : ordersTotalMap.entrySet()){
            csvWriter.append(String.valueOf(entry.getKey()));
            csvWriter.append(",");
            csvWriter.append(String.valueOf(entry.getValue()));
            csvWriter.append("\n");
        }

        csvWriter.flush();
        csvWriter.close();
    }

    public static void saveOrderCustomers() throws IOException {

        FileWriter csvWriter = new FileWriter("./interview/product_customers.csv");
        csvWriter.append("id");
        csvWriter.append(",");
        csvWriter.append("customer_ids");
        csvWriter.append("\n");

        for (Map.Entry<Integer, ArrayList<Integer>> entry : productCustomersMap.entrySet()){
            csvWriter.append(String.valueOf(entry.getKey()));
            csvWriter.append(",");
            String customersListString = entry.getValue().toString().replace("[", "").replace("]", "").replace(", ", " ");
            csvWriter.append(customersListString);
            csvWriter.append("\n");
        }

        csvWriter.flush();
        csvWriter.close();
    }

    public static void saveCustomerRanking() throws IOException {

        FileWriter csvWriter = new FileWriter("./interview/customer_ranking.csv");
        csvWriter.append("id");
        csvWriter.append(",");
        csvWriter.append("firstname");
        csvWriter.append(",");
        csvWriter.append("lastname");
        csvWriter.append(",");
        csvWriter.append("total_euros");
        csvWriter.append("\n");

        for (Map.Entry<Integer, ArrayList<String>> entry : customersRankingOrderedMap){
            int customerId = entry.getKey();
            csvWriter.append(String.valueOf(customerId));
            csvWriter.append(",");
            String customerData = entry.getValue().toString().replace("[", "").replace("]", "").replace(", ", ",");
            csvWriter.append(customerData);
            csvWriter.append("\n");
        }

        csvWriter.flush();
        csvWriter.close();
    }

    public static void loadProducts() {

        BufferedReader br = null;
        String line1 = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader("./interview/products.csv"));
            br.readLine(); //Read and skip the first line

            while ((line1 = br.readLine()) != null) {
                
                String[] csvLineArray = line1.split(cvsSplitBy);

                if(csvLineArray[0] != "" && csvLineArray[1] != "" && csvLineArray[2] != ""){
        
                    int productId = Integer.parseInt(csvLineArray[0]);
                    double productCost = Double.parseDouble(csvLineArray[2]);

                    productsMap.put(productId, productCost); //Populate HashMap
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void loadCustomers() {

        BufferedReader br = null;
        String line1 = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader("./interview/customers.csv"));
            br.readLine(); //Read and skip the first line

            while ((line1 = br.readLine()) != null) {
                
                String[] csvLineArray = line1.split(cvsSplitBy);

                if(csvLineArray[0] != "" && csvLineArray[1] != "" && csvLineArray[2] != ""){
        
                    int customerId = Integer.parseInt(csvLineArray[0]);
                    String firstName = csvLineArray[1];
                    String lastName = csvLineArray[2];
                    //Get the value of the total spent by the customer matching the Id
                    Double totalEuros = customerOrdersMap.get(customerId); 

                    //ArrayList that will contain customer's firstname, lastname and total_euros
                    ArrayList<String> customerData = new ArrayList<String>();

                    customerData.add(firstName);
                    customerData.add(lastName);

                    if(totalEuros == null){
                        totalEuros = 0.0;
                    }

                    customerData.add(String.valueOf(totalEuros));
    
                    customersRankingMap.put(customerId, customerData); //Populate the HashMap
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //Reorder the customer's ranking to desc on total_euros
        customersRankingOrderedMap = new ArrayList<>(customersRankingMap.entrySet());
        Collections.sort(customersRankingOrderedMap, new EntryComparator());
    }

    public static void loadOrders() {

        BufferedReader br = null;
        String line1 = "";
        String cvsSplitBy = ",";

        try {

            br = new BufferedReader(new FileReader("./interview/orders.csv"));
            br.readLine(); //Read and skip the first line

            while ((line1 = br.readLine()) != null) {
                
               String[] csvLineArray = line1.split(cvsSplitBy);

                if(csvLineArray[0] != "" && csvLineArray[1] != "" && csvLineArray[2] != ""){

                    int orderId = Integer.parseInt(csvLineArray[0]);
                    int customerId = Integer.parseInt(csvLineArray[1]);
                    String[] orderProductIdsArray = csvLineArray[2].split(" "); 
                    double orderTotal = 0; 

                    for (String orderProductIdString : orderProductIdsArray){ 
                        
                        int orderProductId = Integer.parseInt(orderProductIdString); //Convert from String to Integer

                        double productCost = productsMap.get(orderProductId); //Get the cost of the product that matches the key

                        orderTotal += productCost; //Sum the cost of the product to the order's total

                        //Add customerId if it's not already present in the HashMap
                        productCustomersMap.computeIfAbsent(orderProductId, k -> new ArrayList<>()).add(customerId);
                    }

                    ordersTotalMap.put(orderId, orderTotal); //Populate the HashMap

                    //Remove duplicates from the HashMap
                    for (Map.Entry<Integer, ArrayList<Integer>> entry : productCustomersMap.entrySet()) {
                        List<Integer> listWithoutDuplicates = entry.getValue().stream().distinct().collect(Collectors.toList());
                        productCustomersMap.put(entry.getKey(), (ArrayList<Integer>) listWithoutDuplicates);
                    }

                    orderTotal = 0;
                    int orderProductId = -1;

                    for (String orderProductIdString : orderProductIdsArray){ 

                        orderProductId = Integer.parseInt(orderProductIdString); //Convert from String to Integer

                        orderTotal += productsMap.get(orderProductId); //Get the price of the product
                    }

                    if(!customerOrdersMap.containsKey(customerId)){ //If customer does not already exist in the HashMap
                        customerOrdersMap.put(customerId, orderTotal);
                    }

                    else if(customerOrdersMap.containsKey(customerId)){ //If customer exists in the HashMap                           
                        double orderOldValue = customerOrdersMap.get(customerId);

                        orderTotal += orderOldValue;

                        customerOrdersMap.replace(customerId, orderOldValue, orderTotal); //Update the order's value
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Class for reordering customer's ranking
    private static class EntryComparator implements Comparator<Map.Entry<Integer, ArrayList<String>>>{

        public int compare(Map.Entry<Integer, ArrayList<String>> left,
            Map.Entry<Integer, ArrayList<String>> right) {    
            //Compare the ArrayList's last element - total_euros
            return Double.compare(Double.valueOf(right.getValue().get(2)), Double.valueOf(left.getValue().get(2)));
        }
    }
}