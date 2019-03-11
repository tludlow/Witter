/**
 * Your preamble here
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
        MyArrayList<Node<Date, User>> usersTraversedDate = this.userDateTree.getNodesTraversed();
        User[] usersReturn = new User[usersTraversedDate.size()];
        for(int i=0; i<usersTraversedDate.size(); i++) {
            usersReturn[i] = usersTraversedDate.get(i).getValue();
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
        private int treeSize;

        //Constructor for the AVLTree, not actually used right now.
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

                //If they are the same we can overwrite the location value with the value of our new node.
                if (comparison <= 0) {
                    //The key of the location node is comparatively less than the node we are inserting, we can recursively call the insert down the left of this tree.
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

            //The node we are trying to find, we will search recursively down the subtrees by calling the second get method below this
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


        //O(n) performance to get all the nodes in the tree by order root, left, right.
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

      public V getValue() {
    	  return this.value;
      }

    //End of the Node class.
}


class MyArrayList<E> {

    private Object[] array;
    private int size;
    private int capacity;

    public MyArrayList() {
        this.capacity = 128;
        this.array = new Object[capacity];
        this.size = 0;
    }

    //A method to add to the array list with automatic resizing in the event that the array capacity matches the size of the array.
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
        return false;
    }


    //Contains - Loops through the array stored in this object and checks for each element against the one provided in the element argument.
    //If they elements match then true is returned, otherwise the loop just finishes and false is returned.
    public boolean contains(E element) {
        for (int i = 0; i < size; i++) {
        	if(this.array[i].equals(element)) {
        		return true;
        	}
        }
        return false;
    }

    public void clear() {
        this.capacity = 100;
        this.array = new Object[capacity];
        this.size = 0;
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    public int size() {
        return size;
    }

    // This line allows us to cast our object to type (E) without any warnings.
    // For further detais, please see: http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/SuppressWarnings.html
    @SuppressWarnings("unchecked")
    public E get(int index) {
        return (E) this.array[index];
    }

    public int indexOf(E element) {
        for (int i=0;i<this.size();i++) {
            if (element.equals(this.get(i))) {
                return i;
            }
        }
        return -1;
    }


    public String toString() {
        if (this.isEmpty()) {
            return "[]";
        }
        StringBuilder ret = new StringBuilder("[");
        for (int i=0;i<size;i++) {
            ret.append(this.get(i)).append(", ");
        }
        ret.deleteCharAt(ret.length()-1);
        ret.setCharAt(ret.length()-1, ']');
        return ret.toString();
    }

}

}
