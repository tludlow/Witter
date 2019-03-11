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
 * @author: u1814232
 */

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.User;

import java.util.Date;


public class UserStore implements IUserStore {

    //An AVL tree of all the users in the system, keyed by their user id because this is unique.
    private AVLTree<Integer, User> userTree = new AVLTree<>();

    //An AVL tree of all the users in the system, keyed by their user date so we can sort by when they were created.
    private AVLTree<Date, User> userDateTree = new AVLTree<>();

    public UserStore() {
        this.userTree = new AVLTree<>();
        this.userDateTree = new AVLTree<>();
    }

    /**
    * addUser() - O(logn) method to create a new user in the system.
    * @param usr - The user to create if not already present
    * @return - True if the user was created in the tree, false otherwise
    */
    public boolean addUser(User usr) {
        //Check for the user already in the system by searching through the AVLTree
        //We only need to check one of the user trees because they should contain the same content just one with a date key and one with their id as a key.
        if(this.userTree.get(usr.getId()) != null) {
            return false;
        } else {
            //The user doesnt already exist, add them in the format specified.
            this.userTree.insertKeyValuePair(usr.getId(), usr);
            this.userDateTree.insertKeyValuePair(usr.getDateJoined(), usr);
            return true;
        }
    }

    /**
    * getUser() - Get a user object from the tree when specified with a user id.
    * @param uid - The users ID for which we want to get the user object for
    * @return - The user object if its present in the tree, null if its not.
    */
    public User getUser(int uid) {
        //We must use the id key tree because these are unique, if we used the date we may return the wrong user.
        return this.userTree.get(uid);
    }

    /**
     * getUsers() - Get an array of user objects from the tree (all the users in the system)
     * @return - The array of users which are in the system, will be an empty array if none are present.
     */
    public User[] getUsers() {
        this.userDateTree.clearNodes();
    	this.userDateTree.inOrderTraversal(this.userDateTree.getRoot());
        MyArrayList<Node<Date, User>> sortedDateList = this.userDateTree.getNodesTraversed();
        User[] usersReturn = new User[sortedDateList.size()];
        for(int i=0; i<sortedDateList.size(); i++) {
            usersReturn[i] = sortedDateList.get(i).getValue();
        }

        return usersReturn;
    }

    /**
     * getUsersContaining() - O(n) method to find all the users in the system who have a certain phrase/word contained within their name 
     * @param query - The query we want to check for in the user's names.
     * @return - A user array of all the users having the query in their name.
     */
    public User[] getUsersContaining(String query) {
    	//Traverse the new tree in order so we get most recent first.
    	this.userDateTree.clearNodes();
    	this.userDateTree.inOrderTraversal(this.userDateTree.getRoot());
        MyArrayList<Node<Date, User>> sortedDateList = this.userDateTree.getNodesTraversed();
        
        //Go through all the users we have found sorted by the date they joined. If they contain the specified query then add them to the arraylist.
        MyArrayList<User> containingUsers = new MyArrayList<>();
        for(int i=0; i<sortedDateList.size(); i++) {
            User user = sortedDateList.get(i).getValue();
            if(user.getName().contains(query)) {
                //The user has the query, add them to the contains arraylist.
                containingUsers.add(user);
            }
        }

    	//Create the storage array which we can return of type User.
    	User[] toReturn = new User[containingUsers.size()];
    	for(int k=0; k<containingUsers.size(); k++) {
    		toReturn[k] = containingUsers.get(k);
    	}
        return toReturn;
    }

    /**
     * getUsersJoinedBefore() - O(n) method to get all the users which joined the system before a provided date.
     * @param dateBefore - The date we want to use to compare against user joined dates. If the user joined before this date we will return them.
     * @return - A user array of all the users joining the system before the date provided.
     */
    public User[] getUsersJoinedBefore(Date dateBefore) {
        //Get all the users in the system sorted by the date they joined.
        this.userDateTree.clearNodes();
    	this.userDateTree.inOrderTraversal(this.userDateTree.getRoot());
        MyArrayList<Node<Date, User>> sortedDateList = this.userDateTree.getNodesTraversed();

        MyArrayList<User> usersBefore = new MyArrayList<>();
        for(int i=0; i<sortedDateList.size(); i++) {
            User user = sortedDateList.get(i).getValue();
            if(user.getDateJoined().before(dateBefore)) {
                //the uer joined before the date specified, we can add them to the return array.
                usersBefore.add(user);
            }
        }

    	//Create the storage array which we can return of type User.
    	User[] toReturn = new User[usersBefore.size()];
    	for(int k=0; k<usersBefore.size(); k++) {
    		toReturn[k] = usersBefore.get(k);
    	}
        return toReturn;
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

}
