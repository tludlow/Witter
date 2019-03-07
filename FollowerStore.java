/**
 * Your preamble here
 *
 * @author: u1814232
 */

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.Weet;
import uk.ac.warwick.java.cs126.models.User;

import java.util.Date;


public class FollowerStore implements IFollowerStore {

        private AVLTree<Integer, AVLTree<Integer, Date>> follows;
	    private AVLTree<Integer, AVLTree<Integer, Date>> followers;

        private MyArrayList<FollowerRanking> followerLeaderboard;

        public FollowerStore() {
        	//Create the new AVLTree objects within their corresponding memory references.
        	this.follows = new AVLTree<>();
        	this.followers = new AVLTree<>();

            this.followerLeaderboard = new MyArrayList<>();
        }

        public boolean addFollower(int uid1, int uid2, Date followDate) {
            //uid1 follows uid2. So uid1 follows should contain uid2 and the uid2 followers should contain uid1. This should not happen if it is already contained.

        	//First we need to make sure the uid1 doesnt follow uid2, this will be done by a tree traversal.
        	//If uid1 follows uid2 already this can be returned false, else add the relationship and true.
        	//We only need to search one of the trees, either follows or followers to known this because they should match.
            if(this.follows.get(uid1) != null && this.follows.get(uid1).get(uid2) != null) {
        		return false;
        	}

        	//Make sure that the the follows tree contains the user of id uid1, if not add them.
        	if(this.follows.get(uid1) == null) {
        		//They arent there, add them with an empty follows tree as a subtree in the node.
        		this.follows.insertKeyValuePair(uid1, new AVLTree<Integer, Date>());
        	}

        	//The follower relationship doesnt already exist, we can add the relationship.
        	//Firstly, lets add uid1 following uid2. Find uid1 in the follows tree and add uid2 to the subtree in the uid1 node.
        	this.follows.get(uid1).insertKeyValuePair(uid2, followDate);


        	if(this.followers.get(uid2) == null) {
        		//They arent there, add them with an empty follows tree as a subtree in the node.
        		this.followers.insertKeyValuePair(uid2, new AVLTree<Integer, Date>());
                //We need to insert this person into the tree because they have no followers.
                this.followerLeaderboard.add(new FollowerRanking(uid2));
        	}

        	//Now need to add the corresponding action to the followers of uid2.
        	//This is essentially the same operation but reversed on the follower tree.
        	this.followers.get(uid2).insertKeyValuePair(uid1, followDate);

            //The user has a follower and we just added them to the leaderboard, lets update their leaderboard to reflect the fact they have a follower at the given date.
            for(int i=0; i<this.followerLeaderboard.size(); i++) {
                FollowerRanking ranking = this.followerLeaderboard.get(i);
                //We should always find a user, there should never be a scenario when the user isnt in the arraylist.
                if(ranking.getUserId() == uid2) {
                    //We have found the ranking update it.
                    ranking.addFollower(followDate);
                }
                
            }

            return true;


        }

        public int[] getFollowers(int uid) {
            //Check that the user actually has followers
            if(this.followers.get(uid) != null) {
                //The user actuallly has follows so we can traverse them and return them.
                AVLTree<Integer, Date> userFollowersTree = this.followers.get(uid);
                userFollowersTree.clearNodes();
                userFollowersTree.inOrderTraversal(userFollowersTree.getRoot());
                MyArrayList<Node<Integer, Date>> followerNodes = userFollowersTree.getNodesTraversed();

                AVLTree<Date, Integer> sortedFollowersTree = new AVLTree<>();
                for(int i=0; i<followerNodes.size(); i++) {
                    sortedFollowersTree.insertKeyValuePair(followerNodes.get(i).getValue(), followerNodes.get(i).getKey());
                }

                sortedFollowersTree.clearNodes();
                sortedFollowersTree.inOrderTraversal(sortedFollowersTree.getRoot());
                MyArrayList<Node<Date, Integer>> sortedFollowersNodes = sortedFollowersTree.getNodesTraversed();

                int[] followsReturned = new int[sortedFollowersNodes.size()];
                for(int i=0; i<sortedFollowersNodes.size(); i++) {
                    followsReturned[i] = sortedFollowersNodes.get(i).getValue();
                }
                return followsReturned;
            }
            return null;
        }


        //TODO maybe take the follow date, integrer key value pair outside also so we can do some easier code for the sorting.
        //TODO maybe take the follow date, integrer key value pair outside also so we can do some easier code for the sorting.
        //TODO maybe take the follow date, integrer key value pair outside also so we can do some easier code for the sorting.
        //TODO maybe take the follow date, integrer key value pair outside also so we can do some easier code for the sorting.
        //TODO maybe take the follow date, integrer key value pair outside also so we can do some easier code for the sorting.


        public int[] getFollows(int uid) {
            //Check that the user actually has followers
            if(this.follows.get(uid) != null) {
                //The user actuallly has follows so we can traverse them and return them.
                AVLTree<Integer, Date> userFollowsTree = this.follows.get(uid);
                userFollowsTree.clearNodes();
                userFollowsTree.inOrderTraversal(userFollowsTree.getRoot());
                MyArrayList<Node<Integer, Date>> followNodes = userFollowsTree.getNodesTraversed();

                AVLTree<Date, Integer> sortedFollowsTree = new AVLTree<>();
                for(int i=0; i<followNodes.size(); i++) {
                    sortedFollowsTree.insertKeyValuePair(followNodes.get(i).getValue(), followNodes.get(i).getKey());
                }

                sortedFollowsTree.clearNodes();
                sortedFollowsTree.inOrderTraversal(sortedFollowsTree.getRoot());
                MyArrayList<Node<Date, Integer>> sortedFollowNodes = sortedFollowsTree.getNodesTraversed();

                int[] followsReturned = new int[sortedFollowNodes.size()];
                for(int i=0; i<sortedFollowNodes.size(); i++) {
                    followsReturned[i] = sortedFollowNodes.get(i).getValue();
                }
                return followsReturned;
            }
            return null;
        }


        public boolean isAFollower(int uidFollower, int uidFollows) {
            if(this.followers.get(uidFollows).get(uidFollower) != null) {
                return true;
            } else {
                return false;
            }
        }

//46
        public int getNumFollowers(int uid) {
            if(this.followers.get(uid) != null) {
                //We can just return the size of the followers tree for the provided user.
                return this.followers.get(uid).getTreeSize();
            }
            return 0;
        }

        public int[] getMutualFollowers(int uid1, int uid2) {
            //Get the followers for the users.
            AVLTree<Integer, Date> uid1Followers = this.followers.get(uid1);
            AVLTree<Integer, Date> uid2Followers = this.followers.get(uid2);

            //Now convert the two trees into a list of the nodes.
            uid1Followers.clearNodes();
            uid2Followers.clearNodes();

            uid1Followers.inOrderTraversal(uid1Followers.getRoot());
            uid2Followers.inOrderTraversal(uid2Followers.getRoot());

            MyArrayList<Node<Integer, Date>> uid1List = uid1Followers.getNodesTraversed();
            MyArrayList<Node<Integer, Date>> uid2List = uid2Followers.getNodesTraversed();

            AVLTree<Date, Integer> sortedMutualTree = new AVLTree<>();

            for(int i=0; i<uid1List.size(); i++) {
                for(int j=0; j<uid2List.size(); j++) {
                    if(uid1List.get(i).getKey() == uid2List.get(j).getKey()) {
                    	//Check the date of the followings so that we add the first one to the sortedMutualTree
                    	if(uid1List.get(i).getValue().equals(uid2List.get(j).getValue())) {
                    		//Doesnt really matter, just going to add uid1 value.
                    		sortedMutualTree.insertKeyValuePair(uid1List.get(i).getValue(), uid1List.get(i).getKey());
                    	} else if (uid1List.get(i).getValue().before(uid2List.get(j).getValue())) {
                    		//uid1 follow before uid2 follow, add uid1
                    		sortedMutualTree.insertKeyValuePair(uid1List.get(i).getValue(), uid1List.get(i).getKey());
                    	} else {
                    		//add uid2
                    		sortedMutualTree.insertKeyValuePair(uid2List.get(j).getValue(), uid2List.get(j).getKey());
                    	}
                    }
                }
            }


            sortedMutualTree.clearNodes();
            sortedMutualTree.inOrderTraversal(sortedMutualTree.getRoot());
            MyArrayList<Node<Date, Integer>> sortedMutualList = sortedMutualTree.getNodesTraversed();
            int[] toReturn = new int[sortedMutualList.size()];
            for(int l=0; l<sortedMutualList.size(); l++) {
                toReturn[l] = sortedMutualList.get(l).getValue();
            }
            return toReturn;
        }

        public int[] getMutualFollows(int uid1, int uid2) {
            //Get the followers for the users.
            AVLTree<Integer, Date> uid1Follows = this.follows.get(uid1);
            AVLTree<Integer, Date> uid2Follows = this.follows.get(uid2);

            //Now convert the two trees into a list of the nodes.
            uid1Follows.clearNodes();
            uid2Follows.clearNodes();

            uid1Follows.inOrderTraversal(uid1Follows.getRoot());
            uid2Follows.inOrderTraversal(uid2Follows.getRoot());

            MyArrayList<Node<Integer, Date>> uid1List = uid1Follows.getNodesTraversed();
            MyArrayList<Node<Integer, Date>> uid2List = uid2Follows.getNodesTraversed();

            AVLTree<Date, Integer> sortedMutualTree = new AVLTree<>();

            for(int i=0; i<uid1List.size(); i++) {
                for(int j=0; j<uid2List.size(); j++) {
                    if(uid1List.get(i).getKey() == uid2List.get(j).getKey()) {
                    	//Check the date of the followings so that we add the first one to the sortedMutualTree
                    	if(uid1List.get(i).getValue().equals(uid2List.get(j).getValue())) {
                    		//Doesnt really matter, just going to add uid1 value.
                    		sortedMutualTree.insertKeyValuePair(uid1List.get(i).getValue(), uid1List.get(i).getKey());
                    	} else if (uid1List.get(i).getValue().before(uid2List.get(j).getValue())) {
                    		//uid1 follow before uid2 follow, add uid1
                    		sortedMutualTree.insertKeyValuePair(uid1List.get(i).getValue(), uid1List.get(i).getKey());
                    	} else {
                    		//add uid2
                    		sortedMutualTree.insertKeyValuePair(uid2List.get(j).getValue(), uid2List.get(j).getKey());
                    	}
                    }
                }
            }

            sortedMutualTree.clearNodes();
            sortedMutualTree.inOrderTraversal(sortedMutualTree.getRoot());
            MyArrayList<Node<Date, Integer>> sortedMutualList = sortedMutualTree.getNodesTraversed();
            int[] toReturn = new int[sortedMutualList.size()];
            for(int l=0; l<sortedMutualList.size(); l++) {
                toReturn[l] = sortedMutualList.get(l).getValue();
            }
            return toReturn;
        }

        public int[] getTopUsers() {
            //Create a tree to sort the data within.
            AVLTree<FollowerRanking, Integer> sortedLeaderboardTree = new AVLTree<>();

            //Collect all of the users from the followerLeaderboard list and themm to a tree for sorting.
            for(int i=0; i<this.followerLeaderboard.size(); i++) {
                FollowerRanking ranking = this.followerLeaderboard.get(i);
                sortedLeaderboardTree.insertKeyValuePair(ranking, ranking.getUserId());
            }

            //Now we have all of our data in a tree, we should be able to inorder traverse this tree and get the correct leaderboard based on the FollowerRanking comparator.
            //Lets do that.
            sortedLeaderboardTree.clearNodes();
            sortedLeaderboardTree.inOrderTraversal(sortedLeaderboardTree.getRoot());
            MyArrayList<Node<FollowerRanking, Integer>> sortedList = sortedLeaderboardTree.getNodesTraversed();

            int[] toReturn = new int[sortedList.size()];
            for(int i=0; i<sortedList.size(); i++) {
                toReturn[i] = sortedList.get(i).getKey().getUserId();
            }

            return toReturn;
        }



    class FollowerRanking implements Comparable<FollowerRanking> {

        private int userId;
        private int followers;
        private Date whenUpdated;

        public FollowerRanking(int userId) {
            this.userId = userId;
            this.followers = 0;
            this.whenUpdated = null;
        }

        public void addFollower(Date dateUpdated) {
            this.followers++;
            //Only update the when updated follower if its before the currently stored date, solves problems of date overwriting on bad conditions
            //Need the null check for scenarios where the date is new because its a first follower
            if(this.whenUpdated == null || dateUpdated.before(this.whenUpdated)) {
                this.whenUpdated = dateUpdated;
            }
        }

        public int getFollowers() {
            return this.followers;
        }

        public int getUserId() {
            return this.userId;
        }

        public Date getLastUpdated() {
            return this.whenUpdated;
        }

        //A method used to compare which followerRanking is higher, very useful.
        @Override
        public int compareTo(FollowerRanking otherRanking) {
            if(this.followers > otherRanking.getFollowers()) {
                return 1;
            } else if (this.followers < otherRanking.getFollowers()) {
                return -1;
            } else {
                //compare the dates the followers match.
                if(this.whenUpdated.before(otherRanking.getLastUpdated())) {
                    return 1;
                } else if (this.whenUpdated.after(otherRanking.getLastUpdated())) {
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
		//We must use add so the list is inserted in ascending order based on the AVLTree comparable.
		this.nodes.add(n); //root
		inOrderTraversal(n.right); //right
    }

    //Method to clear the locally stored nodes linked list.
    public void clearNodes() {
    	this.nodes.clear();
    }

    //getter method for the in order
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
