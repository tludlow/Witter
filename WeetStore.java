/**
 * I have chosen to use an AVL Tree and a ArrayList to implement the required functionality of the WeetStore interface.
 * I have chosen the AVLTree because it is an extension upon the binary search tree, ensuring maximum balance of O(logn) subtrees.
 * This allows for speedy common operations of the following:
 * Insert: O(logn)
 * Search: O(logn)
 * 
 * I have also chosen to use my own ArrayList implementation, taken from the labs as this allows for easy use of an array (with resizing etc)
 * This is good because it has contast get times from the internal array unlike a linked list, which without referncing pointers in a loop is O(n) for basic getting
 * at a specified index.
 * 
 * The memory complexity of my structures is at most, linear without the amount of data stored. The AVL Tree has O(n) memory usage, which is not much on modern computers
 * Likewise, the ArrayList is of O(n) space complexity because as the amount of data stored increases, so does the array.
 * 
 * I could have potentially used a number of possible data structures to implement the required functionality the interfaces required, another fairly ideal solution
 * would be using a HashMap to store the data, which would allow for O(1) access times (assuming the load factor is of good standing). This would allow for constant time
 * operations O(1) but I would have had to write my own Hash function and then it would probably be fairly inefficienct and produce clashes, for which I would have to
 * chain leading to potentially O(n) operations, slower than the O(logn) operations of an AVL Tree which are guaranteed. Furthermore, much of the interface methods require
 * the consideration of all users (all users could have joined before a date, so we have to check them all) and the use of any of the named data structures
 * would not be the bottleneck in the implementations but rather the algorithms themselves.
 * 
 * For some of the implementation I have doubled the use of trees, such as below where I have both an Integer keyed tree and a date keyed Tree. The usage of these is
 * still only O(n) memory as the multiplicative constants are ignored. This allows for simpler method implementations as the sorting of users is already done by the tree
 * requiring no special sorting algorithms, which are probably slower than the O(n) sorting the tree has (lots of O(logn) inserts and then the traversal is O(n))
 * 
 * 
 * For this interface I have also implemented a class for Trend, which is just a handy way of sorting the numerous words starting with a "#". This class implements 
 * the Comparable interface provided by Java and aids in the sorting of the different trending words based on the specification provided in the coursework Javadoc.
 * This is also good as the Tree I have implemented uses comparables to sort the data and by having the Trend class use the comparable interface I can sort the trends
 * as required using a tree and then traversing it.
 *
 * @author: u1814232
 */

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.User;
import uk.ac.warwick.java.cs126.models.Weet;

import java.io.BufferedReader;
import java.util.Date;
import java.io.FileReader;
import java.text.ParseException;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class WeetStore implements IWeetStore {

    //An AVLTree storing the weets indexed as integer keys which are the weet ids.
    //This is used because sometimes you need to get certain weetss given a property about them, this wont work for dates because they could theoretically be non-unique.
    //O(n) storage space based on the number of weets.
    private AVLTree<Integer, Weet> weetStore;

    //An AVLTree storing all the weets indexed by date key, this aids in situations where we need to sort results because an inorder traversal of this tree produces all the weets
    //sorted by the date they occured.
    //O(n) storage space based on the number of weets.
    private AVLTree<Date, Weet> weetByDate;

    //An ArrayList storing the list of trends contained within the weets, making use of the trend object defined below.
    private MyArrayList<Trend> trends;

    public WeetStore() {
        //Instantiate all of the daat structures to be used when the weetstore class is instantiated.
        this.weetStore = new AVLTree<>();
        this.weetByDate = new AVLTree<>();

        this.trends = new MyArrayList<>();
    }

    /**
     * addWeet() - Inserts a new weet object into the witter system, using my data structures.
     * The insertion of the weets is performed in O(logn) time and the weet traversal to store trend data is performed in O(n^2) time depending on the number of trends in the system.
     * This is justified because the quick operations are performed first, and the slow trend updating is performed last, this is a non-blocking operation.
     * In a real world scenario the slow part of this would be added to a queue of tasks and performed on another machine because trends to weet existence does not need to be 100%
     * exact as soon as the weets occur, it can be somewhat delayed in updating because trends dont update very often in a real system.
     * 
     * @param weet - A Weet object which we want to store in the data structures we have created.
     * @return true in the case where the weet is stored, false in the case where the weet isnt stored as one already exists with this exact object equality.
     */
    public boolean addWeet(Weet weet) {
        if(this.weetStore.get(weet.getId()) != null) {
            return false;
        } else {
            //The weet doesnt exist in the system, store it in both of the AVLTree's.
            this.weetStore.insertKeyValuePair(weet.getId(), weet);
            this.weetByDate.insertKeyValuePair(weet.getDateWeeted(), weet);

            //We also now want to scan the weet for all the trend's inserted into them.
            //This is done now, based on the justification above, rather than at the time when trends need to be found in the system.

            //Split the weet down into its component words so we can find all trending words.
            String[] weetWords = weet.getMessage().split(" ");

            wordsLoop:
            for(int i=0; i<weetWords.length; i++) {
                if(weetWords[i].substring(0, 1).equals("#")) {
                    //This is a trend, find the trend in the trend arraylist.
                    for(int j=0; j<this.trends.size(); j++) {
                        Trend trend = this.trends.get(j);
                        if(weetWords[i].equals(trend.getMessage())) {
                            //The trend message and the word in the tweet match, we can update the trend occurences.
                            this.trends.get(j).addOccurence(weet.getDateWeeted());
                            return true;
                        }
                    }

                    //We never found the weet we wanted, lets add it to the trend list as its assumed its a new trend in the system.
                    this.trends.add(new Trend(weetWords[i], weet.getDateWeeted()));
                }
            }

            return true;
        }
    }

    /**
     * getWeet() - Performs in O(logn) time as it performs a search on the AVLTree. Makes use of the keyed tree of integers because they are unique and cant produce
     * unambiguous values where key's are equal across multiple nodes.
     * 
     * @param wid - A weet id, for which we want to find the weet object off.
     * @return - The weet which as the id provided in the argument, or null if it doesnt exist in the data store.
     */
    public Weet getWeet(int wid) {
        return this.weetStore.get(wid);
    }

    /**
     * getWeets() - O(n) method traversing all the nodes in the weet tree.
     * @return - An array of all the weets
     */
    public Weet[] getWeets() {
        //Traverse the AVLTree containing all the weets with the key being the date (this means we dont need to sort the weets)
        this.weetByDate.clearNodes();
        this.weetByDate.inOrderTraversal(this.weetByDate.getRoot());
        MyArrayList<Node<Date, Weet>> foundByDate = this.weetByDate.getNodesTraversed();

        //Create the return array with a fixed size matching the number of weets.
        Weet[] weetsReturn = new Weet[foundByDate.size()];
        //Go across all the nodes traversed from the tree, extract the weet information into the return array.
        for(int j=0; j<foundByDate.size(); j++) {
            weetsReturn[j] = foundByDate.get(j).getValue();
        }
        return weetsReturn;
    }

    /** 
     * getWeetsByUser() - O(n) method to find all the weets created by a given user.
     * @param usr - The user which we want to find all the weets by.
     * @return - An array of weets containing those which the user argument created.
    */
    public Weet[] getWeetsByUser(User usr) {
        this.weetByDate.clearNodes();
        this.weetByDate.inOrderTraversal(this.weetByDate.getRoot());
        MyArrayList<Node<Date, Weet>> foundWeets = this.weetByDate.getNodesTraversed();

        //Now we need to check the weets to check if the user who weeted was the one we want.
        MyArrayList<Weet> foundByUser = new MyArrayList<>();
        for(int i=0; i<foundWeets.size(); i++) {
            Node<Date, Weet> node = foundWeets.get(i);
            if(node.getValue().getUserId() == usr.getId()) {
                foundByUser.add(node.getValue());
            }
        }

        //Now go over the arraylist of nodes in order of date and add them to the weet[]
        Weet[] weetReturn = new Weet[foundByUser.size()];
        for(int k=0; k<foundByUser.size(); k++) {
            weetReturn[k] = foundByUser.get(k);
        }
        return weetReturn;
    }

    
    /**
     * getWeetsContaining() - O(n) method for finding all the weets containing a specific phrase.
     * @param query - The word/phrase which we are looking for in the weets to return.
     * @return - An array of weets where the weets contain the query provided in the argument.
     */
    public Weet[] getWeetsContaining(String query) {
    	this.weetByDate.clearNodes();
    	this.weetByDate.inOrderTraversal(this.weetByDate.getRoot());
    	MyArrayList<Node<Date, Weet>> weetsFound = this.weetByDate.getNodesTraversed();

        MyArrayList<Weet> weetsContaining = new MyArrayList<>();
        for(int i=0; i<weetsFound.size(); i++) {
            if(weetsFound.get(i).getValue().getMessage().contains(query)) {
                weetsContaining.add(weetsFound.get(i).getValue());
            }
        }

        Weet[] toReturn = new Weet[weetsContaining.size()];
        for(int i=0; i<weetsContaining.size(); i++) {
        	toReturn[i] = weetsContaining.get(i);
        }
        return toReturn;
    }

    /**
     * getWeetsOn() - O(n) method for finding all the weets created on a given day.
     * @param dateOn - The date which we want to find all the weets which are posted on the day provided.
     * @return - All the weets which were created on the day provided in the dateOn argument.
     */
    public Weet[] getWeetsOn(Date dateOn) {
        //Get all the nodes of the weet tree. aka all the weets
        this.weetByDate.clearNodes();
        this.weetByDate.inOrderTraversal(this.weetByDate.getRoot());
        MyArrayList<Node<Date, Weet>> nodesFound = this.weetByDate.getNodesTraversed();

        MyArrayList<Weet> weetsOn = new MyArrayList<>();
        for(int i=0; i<nodesFound.size(); i++) {
            Node<Date, Weet> node = nodesFound.get(i);
            if(node.getKey().getDay() == dateOn.getDay()) {
                weetsOn.add(node.getValue());
            }
        }

        //Create the return array.
        Weet[] weetReturn = new Weet[weetsOn.size()];
        for(int j=0; j<weetsOn.size(); j++) {
            weetReturn[j] = weetsOn.get(j);
        }
        return weetReturn;

    }

    
    /**
     * getWeetsBefore() - A method to find all the weets before a given date.
     * //Time efficiency: O(n) - very hard to get it any quicker as you have to consider every weet having the possibility of being before the argument date.
     * @param dateBefore - The date for which we want to return all the weets before it.
     * @return - A weet array of all the weets before the date provided in the argument. The returned weets are sorted so that the most recent weet is first.
     */
    public Weet[] getWeetsBefore(Date dateBefore) {
        //Use the getWeets method to find all the weets in the system sorted by date.
        Weet[] allWeets = getWeets();

        //Iterate across al the weets, checking if the weet is before the date provided in the argument.
        //All the weets found with the condition are placed into the arraylist as its dynamically sized.
        MyArrayList<Weet> weetsBefore = new MyArrayList<>();
        for(int i=0; i<allWeets.length; i++) {
            Weet weet = allWeets[i];
            if(weet.getDateWeeted().before(dateBefore)) {
                weetsBefore.add(weet);
            }
        }

        //All of the weets found within the weetsBefore should already be sorted as we got them sorted originally.
        //We just now need to transform the arraylist of weets to an array of weets.
        Weet[] toReturn = new Weet[weetsBefore.size()];
        for(int i=0; i<weetsBefore.size(); i++) {
            toReturn[i] = weetsBefore.get(i);
        }
        return toReturn;
    }

    /**
     * getTrending() - Gets the most popular trends from the weets and presents it to the UI on the frontend.
     * O(n) performance based on the number of trends in the trends arraylist.
     * @return A fixed size String array of the 10 most popular trends based on the criteria specified in the coursework javadocs.
     */
    public String[] getTrending() {
        //The template array to return of fixed length 10. All values intialised to null because this is required when a trend isnt present.
		String[] toReturn = new String[]{null, null, null, null, null, null, null, null, null, null};

        //Creation of a Trend object keyed tree so we can order the trends based on the comparator they implement. The trend tree is loaded with the data from the trend arraylist.
        AVLTree<Trend, String> trendTree = new AVLTree<>();
        for(int i=0; i<this.trends.size(); i++) {
            trendTree.insertKeyValuePair(this.trends.get(i), this.trends.get(i).getMessage());
        }

        //Traverse the tree and store the nodes within an arraylist so we extract the data and present it to the user in the return array.
        trendTree.clearNodes();
        trendTree.inOrderTraversal(trendTree.getRoot());
        MyArrayList<Node<Trend, String>> sortedTrends = trendTree.getNodesTraversed();

        //Iterate across the 10 most popular trends and add the weet message, aka the hashtag to the return array.
        for(int i=0; i<10; i++) {
            toReturn[i] = sortedTrends.get(i).getKey().getMessage();
        }

        return toReturn;
    }

    /**
     * Trend class, used to store data about a trend aka a string beginning with a hashtag from a tweet.
     * Implemented using the Comparable interface so I can define which Trend takes priority on the "leaderboard".
     * The comparisons are made based on the number of tweet occurences, and if that is equal then the date it was last tweeted.
     */
    class Trend implements Comparable<Trend> {

        //The actual tweet, aka #cs126
        private String message;
        //The last time this was tweeted (the most recent tweet date containing the trend)
        private Date updatedAt;
        //The number of times this trend has been tweeted.
        private int occurences;

        public Trend(String message, Date date) {
            this.message = message;
            this.updatedAt = date;
            this.occurences = 1;
        }

		public void addOccurence(Date date) {
            this.occurences++;
            //here we should only update the date of the occurence if its newer than the one currently stored, this is needed as the weets arent loaded in order.
			if(date.after(this.updatedAt)) {
                //update the date the condition is met.
                this.updatedAt = date;
            }
		}

        public String getMessage() {
            return this.message;
        }

        public Date getUpdatedAt() {
            return this.updatedAt;
        }

        public int getOccurences() {
            return this.occurences;
        }

        //The implemented method of the Comparable interface which allows for comparisons between trends.
        //If one trend has more ocucrences than the other it wins, else we check the dates with the most recent one taking priority.
        @Override
        public int compareTo(Trend otherTrend) {
            if(this.occurences > otherTrend.getOccurences()) {
                return 1;
            }
            else if (this.occurences < otherTrend.getOccurences()) {
                return -1;
            } else {
                //We need to compare dates.
                if(this.updatedAt.before(otherTrend.getUpdatedAt())) {
                    return -1;
                }
                else if (this.updatedAt.after(otherTrend.getUpdatedAt())) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }


    class AVLTree<K extends Comparable<K>, V> {

        //The root node of this tree. The single node at the top for which every other node stems from.
        private Node root;
        //the number of nodes within the tree (root = 1)
        private int treeSize;

        //Constructor for the tree, just sets the treesize to an intiial amount which can be configured.
        public AVLTree() {
            this.treeSize = 0;
        }

        public Node getRoot() {
            return this.root;
        }

        public int getTreeSize() {
            return this.treeSize;
        }

        public void setRoot(Node n) {
            this.root = n;
        }

        public void setTreeSize(int size) {
            this.treeSize = size;
        }

        //O(logn) insertion in this tree as we need to traverse the tree to find the right location.
        private Node insertNode(Node locationNode, Node insertingNode) {
            if (locationNode != null) {
                //Compare the location node we intend to the key of the node we are inserting.
                int comparison = ((Comparable<K>) locationNode.key).compareTo((K) insertingNode.key);

                //If the value is less than or equal to the current node we are checking put it on the left.
                if (comparison <= 0) {
                    //The key of the location node is comparatively less or equal to the node we are inserting, we can recursively call the insert down the left of this tree.
                    locationNode.left = insertNode(locationNode.left, insertingNode);
                } else {
                    //The key of the location node is comparatively more than the node we are inserting, we can recursively call the insert down the right of this tree.
                    locationNode.right = insertNode(locationNode.right, insertingNode);
                }

                //We have just inserted data into this nodes subtree somewhere, we should rebalance this node now so it maintains optimal data structure efficiency.
                return locationNode.balanceNode();
            } else {
                return insertingNode;
            }
        }

        //O(logn) peroformance as it just makes a call to the function insertNode()
        public void insertKeyValuePair(K key, V value) {
            this.treeSize++;
            root = insertNode(root, new Node<>(key, value));
        }

        //Check if the tree has data within it or not by comparing the root value to null as everything stems from this node.
        public boolean isEmpty() {
            return this.root == null;
        }

        //Clear the tree of all its nodes by deleting the greatest parent (root node)
        public void clearTree() {
            this.root = null;
        }


        //O(logn) performance as we traverse the tree to find the value found at the key, the maximum height of the tree is logn of the numbers of nodes.
        public V get(K key) {
            //The key isnt provided? return null we cant search for nothing.
            if (key == null) {
                return null;
            }

            //The node we are trying to find, we will search recursively down the subtrees by calling the second get method below this (the private recursive method)
            Node toFind = get(root, key);
            if (toFind == null) {
                return null;
            }

            //We found the node we were looking for, return the value associated within it as the recursive searched is only returning a node for abstraction purposes if needed later.
            return (V) toFind.value;
        }

        private Node get(Node currentNode, K key) {
            //Cant search against nothing, return null to show an error occured.
            if (currentNode == null)
                return null;

            //The comparison we are making between the the key we have and the key we are looking for in the avl tree structure
            int comparison = ((Comparable<K>) currentNode.key).compareTo(key);

            if (comparison < 0) {
                //The node we are looking for has a key less than the current node, we should go down the left subtree.
                return get(currentNode.left, key);
            } else if (comparison > 0) {
                //The key we are searching for has a value greater than the key at the current node, search down the right subtree.
                return get(currentNode.right, key);
            } else {
                //We are at the node (comparison == 0) we are looking for, return this node.
                return currentNode;
            }
        }


        //Internal storage for the nodes we have traversed in the tree, if this was to be improved I would abstract this outside of the tree so we can define
        //search paramaters elsewhere in the program to stop the need for having to iterate across all the nodes again once traversed to see which ones match the condition.
        private MyArrayList<Node<K, V>> nodes = new MyArrayList<>();

        //O(n) performance to get all the nodes in the list in ascending order by key.
        public void inOrderTraversal(Node n) {
            //The node doesnt exist, we cant traverse this.
            if(n == null) {
                return;
            }

            inOrderTraversal(n.left); //left
            //We must use addToTail so the list is inserted in ascending order based on the AVLTree comparable.
            this.nodes.add(n); //root
            inOrderTraversal(n.right); //right
        }

        //Method to clear the locally stored nodes linked list.
        public void clearNodes() {
            this.nodes.clear();
        }

        //Getter method for the array found above the preOrderTraversal() method because the data for this is stored there.
        public MyArrayList<Node<K, V>> getNodesTraversed() {
            return this.nodes;
        }

    //End of the AVLTree class
    }

    class Node<K, V> {
        //The data associated with the Node, the key value pair with the key being the Weet id and the value being the Weet
        //Slightly inefficient use for memory as I am storing the id twice but its a negligible amount for the scale of this project.
        public K key;
        public V value;

        //The pointers to the nodes surrounding this node, the sibling nodes and the parent.
        public Node left;
        public Node right;
        public Node parent;

        //A useful variable to store the height of the subtree with this node as its parent, not strictly needed but helpful
        //The number of nodes at both the left of this node and the right of this node summed.
        public int height;

        //Constructor to instantiate a new node within the AVL Tree.
        public Node(K key, V value) {
            this.key = key;
            this.value = value;
            this.height = 1;
        }

        //A method to perform a right rotation on the tree to aid in its balancing.
        //Just follows the procedures defined in the ADT specification.
        //Rotations are ran in O(logn) time.
        public Node rotateRight() {
            //Store the left node temporarily, we will overwrite it but also want it again in the future.
            Node tempNode = this.left;

            //Update the current left node to the be the current right node.
            this.left = tempNode.right;
            this.calculateNodeHeight();

            //Set the current right node to this node, (the parent of the current right node)
            tempNode.right = this;
            tempNode.calculateNodeHeight();

            //Upward shift of the nodes.
            tempNode.parent = this.parent;

            //Return an updated version of this node after having been rotated.
            return tempNode;
        }

        //A left rotation on this node, aiding in the self balance of the tree to ensure O(logn) search times.
        //Just follows the principles defined in the ADT definition.
        //Rotations are ran in O(logn) time.
        public Node rotateLeft() {
            Node tempNode = this.right;

            //Perform the leftward balance, making the current right node to be the current left node.
            this.right = tempNode.left;
            this.calculateNodeHeight();

            //second step in this balancing maneoveur, changing the left node of this tree to be itself.
            tempNode.left = this;
            tempNode.calculateNodeHeight();

            //We now return the node again but the balanced version of it.
            return tempNode;
        }

        //Recalculates this nodes height from the top down in both the left and right subtrees.
        public void calculateNodeHeight() {
            int subTreeHeight = 1;

            if (left != null)
                subTreeHeight += left.height;

            if (right != null)
                subTreeHeight += right.height;

            //Overwrite the current class height variable to be the newly computed height of this node.
            this.height = subTreeHeight;
        }

        //Returns the balance between this nodes left subtree and right subtree (the difference in heights between them)
        private int calculateBalance() {
            //Calculate the left height of this node.
            int leftHeight = 0;
            if (left != null)
                leftHeight += left.height;

            //Calculate the right height of this node
            int rightHeight = 0;
            if (right != null)
                rightHeight += right.height;

            //Returns the difference between the two heights, producing our "balance"
            return leftHeight - rightHeight;
        }


        //Balance the tree so that at most the tree is of O(logn) height. The tree can baalnce itself in O(logn) time as defined in the ADT specification.
        public Node balanceNode() {
            //Update the current node height, good practice so we can accuratley perform the following operations.
            this.calculateNodeHeight();

            int currentNodeBalance = this.calculateBalance();

            //Check for a right skew on this nodes data, if so we should balance towards the left.
            if (currentNodeBalance > 1) {
                if (this.left.calculateBalance() < 0) {
                    this.left = this.left.rotateLeft();
                }
                return rotateRight();

            //Check for a left skew, if so balance towards the right.
            } else if (currentNodeBalance < -1) {
                if (this.right.calculateBalance() > 0) {
                    this.right = this.right.rotateRight();
                }
                return rotateLeft();
            }

            //Return this node again in its balanced form.
            //This could also be potentially called if the node had no skew, and is therefore just returning itself
            return this;
        }

        //Getters for the generic values found within the node. Used often when a traversal has occured and we want to get the data out from the tree.
        public V getValue() {
            return this.value;
        }

        public K getKey() {
            return this.key;
        }

        //End of the Node class.
    }


    class MyArrayList<E> {

        //the internal object array which holds all of our elements.
        private Object[] array;
        //the number of elements currently being held by the arraylist.
        private int size;
        //the maximum amount of elements we can currently hold without resizing the internal array.
        private int capacity;

        public MyArrayList() {
            this.capacity = 128;
            this.array = new Object[capacity];
            this.size = 0;
        }

        /**
         * add() - O(1) method to add a new element to the arraylist at the next available position, in the case where the internal array is full a full copy of the arraylist is performed to double its capacity.
         * In the situation where this occurs the add method runs in O(n) time.
         * @param element - The element we are trying to add to the arraylist
         * @return true when the element is added, false otherwise (should never actually be false).
         */
        public boolean add(E element) {
            if (this.size == this.capacity) {
                //The size is the same as the capacity we will have to resize the array, copy all elements over to the new array and then overwrite the old array and redefine the new capacity.
                Object[] newArray = new Object[this.capacity * 2];
                for (int i = 0; i < size; i++) {
                    newArray[i] = this.array[i];
                }
                this.array = newArray;
                this.capacity = this.capacity * 2;
                this.array[size] = element;
                this.size++;
            } else {
                this.array[size] = element;
                this.size++;
            }
            return true;
        }


        /**
         * contains() - O(n) method that checks if the provided element is contained within the arraylist.
         * @param element - The element to check for in the arraylist.
         * @return - true if the element being checked exists, false otherwise.
         */
        public boolean contains(E element) {
            for (int i=0; i<size; i++) {
                //Must use a .equals rather than == because it may not be off a primitve type due to the generic nature of the arraylist.
                if(this.array[i].equals(element)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * clear() - O(1) method that resets the arraylist back to its post instantiated state, not used too often.
         */
        public void clear() {
            this.capacity = 128;
            this.array = new Object[capacity];
            this.size = 0;
        }

        /**
         * isEmpty() - O(1) method to check if the arraylist actually contains some elements.
         */
        public boolean isEmpty() {
            return this.size() == 0;
        }

        /**
         * size() - Essentially just a getter for the private size variable and is therefore O(1).
         */
        public int size() {
            return size;
        }

        /**
         * get() - O(1) method that returns the element contained at the index provieed.
         * @param index - The position in the arraylist we want to get the data for.
         * @return the element we have found in the arraylist at the provided index, the type of this element is E.
         */
        // This line allows us to cast our object to type (E) without any warnings.
        // For further detais, please see: http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/SuppressWarnings.html
        @SuppressWarnings("unchecked")
        public E get(int index) {
            return (E) this.array[index];
        }

        /**
         * indexOf() - O(n) method that finds the index of an element in the arraylist when provided with the element itself.
         * @param element - The element we want to find the index for.
         * @return - The integer value of the index where this element exists in the arraylist.
         */
        public int indexOf(E element) {
            for (int i=0;i<this.size();i++) {
                if (element.equals(this.get(i))) {
                    return i;
                }
            }
            return -1;
        }
    //End of the arraylist class.
    }
//End of weetstore class.
}
