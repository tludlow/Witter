/**
 * Your preamble here
 *
 * @author: Your university ID
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

    private AVLTree<Integer, Weet> weetStore;
    private AVLTree<Date, Weet> weetByDate;

    private MyArrayList<Trend> trends;

    public WeetStore() {
        this.weetStore = new AVLTree<>();
        this.weetByDate = new AVLTree<>();

        this.trends = new MyArrayList<>();
    }

    /**
     * Time efficiency: O(logn) - O(logn) check for existence in the tree, if the weet with the id provided doesnt exist we can add it twice 2 * O(logn) operations (insertion into AVLTree)
     * I check for the weet existing in the weet id key tree because this is a unique value, a weets date might not be unique.
     *
     */
    public boolean addWeet(Weet weet) {
        if(this.weetStore.get(weet.getId()) != null) {
            return false;
        } else {
            this.weetStore.insertKeyValuePair(weet.getId(), weet);
            this.weetByDate.insertKeyValuePair(weet.getDateWeeted(), weet);

            //We also now need to scan the weet for trends, because if they have trends we need to add it to the trend "leaderboard"
            //In the case which the trend is new, otherwise update the trend occurences.
            String[] weetWords = weet.getMessage().split(" ");
            //Now go over the word, checking it the word starts with a #
            wordsLoop:
            for(int i=0; i<weetWords.length; i++) {
                if(weetWords[i].substring(0, 1).equals("#")) {
                    //THis is a trend, find the trend in the trend arraylist.
                    for(int j=0; j<this.trends.size(); j++) {
                        Trend trend = this.trends.get(j);
                        if(weetWords[i].equals(trend.getMessage())) {
                            //The trend message and the word in the tweet match, we can update the trend occurences.
                            this.trends.get(j).addOccurence(weet.getDateWeeted());
                            return true;
                        }
                    }

                    //We never found the weet we wanted, lets add it to the trend list.
                    this.trends.add(new Trend(weetWords[i], weet.getDateWeeted()));
                }
            }

            return true;
        }
    }

    public Weet getWeet(int wid) {
        return this.weetStore.get(wid);
    }

    public Weet[] getWeets() {
        //Now to traverse the new tree so that we can get all the weets by the date they were added.
        this.weetByDate.clearNodes();
        this.weetByDate.inOrderTraversal(this.weetByDate.getRoot());
        MyArrayList<Node<Date, Weet>> foundByDate = this.weetByDate.getNodesTraversed();

        //Create the array were going to return, and then populate this array from the arraylist.
        Weet[] weetsReturn = new Weet[foundByDate.size()];
        for(int j=0; j<foundByDate.size(); j++) {
            weetsReturn[j] = foundByDate.get(j).getValue();
        }
        return weetsReturn;
    }

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

    //O(n)
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

    public Weet[] getWeetsBefore(Date dateBefore) {
        //Get all the nodes of the weet tree. aka all the weets
        this.weetByDate.clearNodes();
        this.weetByDate.inOrderTraversal(this.weetByDate.getRoot());
        MyArrayList<Node<Date, Weet>> nodesFound = this.weetByDate.getNodesTraversed();

        MyArrayList<Weet> weetsBefore = new MyArrayList<>();
        for(int i=0; i<nodesFound.size(); i++) {
            Node<Date, Weet> node = nodesFound.get(i);
            if(node.getKey().before(dateBefore)) {
                weetsBefore.add(node.getValue());
            }
        }

        //Create the return array.
        Weet[] weetReturn = new Weet[weetsBefore.size()];
        for(int j=0; j<weetsBefore.size(); j++) {
            weetReturn[j] = weetsBefore.get(j);
        }
        return weetReturn;
    }

    public String[] getTrending() {
		String[] toReturn = new String[]{null, null, null, null, null, null, null, null, null, null};

        //Now we need to sort the trends array based on the trend comparator implemented so we can find the order we desire.
        //We can do this by adding them to a tree and in order traversing it for the results.
        AVLTree<Trend, String> trendTree = new AVLTree<>();
        for(int i=0; i<this.trends.size(); i++) {
            trendTree.insertKeyValuePair(this.trends.get(i), this.trends.get(i).getMessage());
        }


        trendTree.clearNodes();
        trendTree.inOrderTraversal(trendTree.getRoot());
        MyArrayList<Node<Trend, String>> sortedTrends = trendTree.getNodesTraversed();

        //go through the sorted trends add them to the return array.
        for(int i=0; i<10; i++) {
            toReturn[i] = sortedTrends.get(i).getKey().getMessage();
        }

        return toReturn;
    }

    class Trend implements Comparable<Trend> {

        private String message;
        private Date updatedAt;
        private int occurences;

        public Trend(String message, Date date) {
            this.message = message;
            this.updatedAt = date;
            this.occurences = 1;
        }

        public int getOccurences() {
            return this.occurences;
        }

        public String getMessage() {
            return this.message;
        }

        public Date getUpdatedAt() {
            return this.updatedAt;
        }

        public void addOccurence(Date date) {
            this.occurences++;
            this.updatedAt = date;
        }

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
                    return 1;
                }
                else if (this.updatedAt.after(otherTrend.getUpdatedAt())) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
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

    //A function to print the current situation of the tree, good for testing.
    //DELETE BEFORE SUBMISSION, NOT NEEDED.
    public void dump(Node node, int level) {
		if (node == null) {
			return;
		}

		dump(node.left, level + 1);
		for (int i = 0; i < level; ++i) {
			System.out.print('\t');
		}

		System.out.println(node.key + " " + node.value);
		dump(node.right, level + 1);
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

      public K getKey() {
          return this.key;
      }

    //End of the Node class.
}


class MyArrayList<E> {

    private Object[] array;
    private int size;
    private int capacity;

    public MyArrayList() {
        this.capacity = 128;
        this.array = new Object[this.capacity];
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
