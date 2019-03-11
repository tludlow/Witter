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
 * This class makes use of the FollowerRanking class, much like the Trend class from WeetStore. Having this allows for the sorting of all the users follower rankings as required
 * in the getTopUsers() method. It implements a compareTo() function which is specified in the coursework javadocs so that the users can be sorted by their ranking appropriatley.
 * 
 * One thing to note is that for this class I have trees inside of trees. This is O(n^2) space complexity however the number of users following/followed will be a small subset
 * of the overall number of users in the system, so the size should not grow too big. Another way to possible implement this is to use sets, because follower relationships must be
 * unique, and then the mutual methods could have intersection methods which would be quicker than the O(n^2) implementations as of current.
 *
 * @author: u1814232
 */

package uk.ac.warwick.java.cs126.services;

import uk.ac.warwick.java.cs126.models.Weet;
import uk.ac.warwick.java.cs126.models.User;

import java.util.Date;


public class FollowerStore implements IFollowerStore {

    //A tree which holds all the follows a user has (their id), which holds all the follows in its own tree.
    private AVLTree<Integer, AVLTree<Integer, Date>> follows;
    //A tree which holds all the followers a user has (their id), which holds all the followers in its own tree.
    private AVLTree<Integer, AVLTree<Integer, Date>> followers;

    //An arraylist holding the follower rankings of all users who have followers, sorting this using the FollowerRankign Comparable will sort the top followed users.
    private MyArrayList<FollowerRanking> followerLeaderboard;

    public FollowerStore() {
        //Create the new trees so they are in memory, same for the follower leaderboard
        this.follows = new AVLTree<>();
        this.followers = new AVLTree<>();

        this.followerLeaderboard = new MyArrayList<>();
    }

    /**
     * addFollower() - Create the follower relationship between two users, where uid1 follows uid2.
     * O(logn) checks of the follower relationships, and needed updates. O(n) update of the follower ranking leaderboard.
     * You could take the follower ranking part out and put it in a queue of jobs, which another computer could process (in an ideal real world implementation)
     * @param uid1 - The user following
     * @param uid2 - The user being followed by the following user
     * @param followDate - The date which the follow relationship occured.
     * @return - True if the follower relationship was created, false otherwise (it probably already exists).
     */
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

    /**
     * getFollowers() - O(n) (scaling with the amount of users) method for gettting all the users which follow the provided user id.
     * @param uid - The user we want to get all of the followers for.
     * @return - null when no followers exist for the provided user, else an array of integer representing all the follower user ids. (sorted by the date of the following)
     */
    public int[] getFollowers(int uid) {
        //Check that the user actually has followers
        if(this.followers.get(uid) != null) {
            //The user actuallly has follows so we can traverse them and return them.
            AVLTree<Integer, Date> userFollowersTree = this.followers.get(uid);
            userFollowersTree.clearNodes();
            userFollowersTree.inOrderTraversal(userFollowersTree.getRoot());
            MyArrayList<Node<Integer, Date>> followerNodes = userFollowersTree.getNodesTraversed();

            //We want all the followers sorted by the date they follows, we will add them to a date keyed tree and traverse this for the right order
            //This is still only a O(n) operation.
            AVLTree<Date, Integer> sortedFollowersTree = new AVLTree<>();
            for(int i=0; i<followerNodes.size(); i++) {
                sortedFollowersTree.insertKeyValuePair(followerNodes.get(i).getValue(), followerNodes.get(i).getKey());
            }

            sortedFollowersTree.clearNodes();
            sortedFollowersTree.inOrderTraversal(sortedFollowersTree.getRoot());
            MyArrayList<Node<Date, Integer>> sortedFollowersNodes = sortedFollowersTree.getNodesTraversed();

            //Traverse the sorted date tree and extract the uid's from the nodes to return.
            int[] followsReturned = new int[sortedFollowersNodes.size()];
            for(int i=0; i<sortedFollowersNodes.size(); i++) {
                followsReturned[i] = sortedFollowersNodes.get(i).getValue();
            }
            return followsReturned;
        }
        return null;
    }


    /**
     * getFollows() - O(n) method to get all the follows of a user (scaling with the amount of users)
     * @param uid - The user id of the user we want to get all the follows for.
     * @return - null if there are no follows, else an array of integers where each integer is a user the provided uid follows (sorted by the date of the follow)
     */
    public int[] getFollows(int uid) {
        //Check that the user actually has followers
        if(this.follows.get(uid) != null) {
            //The user actuallly has follows so we can traverse them and return them.
            AVLTree<Integer, Date> userFollowsTree = this.follows.get(uid);
            userFollowsTree.clearNodes();
            userFollowsTree.inOrderTraversal(userFollowsTree.getRoot());
            MyArrayList<Node<Integer, Date>> followNodes = userFollowsTree.getNodesTraversed();

            //We now want to add all the users back to a tree sorted by date key so we can sort them.
            AVLTree<Date, Integer> sortedFollowsTree = new AVLTree<>();
            for(int i=0; i<followNodes.size(); i++) {
                sortedFollowsTree.insertKeyValuePair(followNodes.get(i).getValue(), followNodes.get(i).getKey());
            }

            sortedFollowsTree.clearNodes();
            sortedFollowsTree.inOrderTraversal(sortedFollowsTree.getRoot());
            MyArrayList<Node<Date, Integer>> sortedFollowNodes = sortedFollowsTree.getNodesTraversed();

            //Get all the sorted follows by date put of its tree and add to the return array as uid's and not Nodes
            int[] followsReturned = new int[sortedFollowNodes.size()];
            for(int i=0; i<sortedFollowNodes.size(); i++) {
                followsReturned[i] = sortedFollowNodes.get(i).getValue();
            }
            return followsReturned;
        }
        return null;
    }


    /**
     * isAFollower() - O(logn) operation search an AVLTree to find out if a follower relationship is present.
     * We only need to look down one of the trees at the top because we have a symmetric relationship between followers 
     * @param uidFollower - The person who is following
     * @param uidFollows - The person who is followed
     * @return true if uidFollower follows uidFollows, else false.
     */
    public boolean isAFollower(int uidFollower, int uidFollows) {
        if(this.followers.get(uidFollows).get(uidFollower) != null) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * getNumFollowers() - O(logn) method to search a binary tree, we then read the trees size variable to get the size, which is incremented everytime a node
     * is added to the tree, this saves us having to traverse the tree and count the size of the arraylist returned (memory vs time tradeoff)
     * @param uid - The user we want to find the size of their follower tree (how many followers they have)
     * @return - An int count of the number of followers a user has which is provided as the argument.
     */
    public int getNumFollowers(int uid) {
        //Check if the user actually has follows, to prevent nullPointerExceptions
        if(this.followers.get(uid) != null) {
            ///The user actually has follows, find out how many by reading the treesize variable from their follower tree.
            return this.followers.get(uid).getTreeSize();
        }
        return 0;
    }

    /**
     * getMutualFollowers() - O(n^2) method for finding common elements between two arraylists of followers.
     * @param uid1 - User 1 we want the followers for
     * @param uid2 - User 2 we want the followers for
     * @return - An integer array of user id's where the users are followers of both uid1 and uid2
     */
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

        //A tree to store the mutual followers in, keyed by date so we have the relationships sorted.
        AVLTree<Date, Integer> sortedMutualTree = new AVLTree<>();

        for(int i=0; i<uid1List.size(); i++) {
            for(int j=0; j<uid2List.size(); j++) {
                if(uid1List.get(i).getKey() == uid2List.get(j).getKey()) {
                    //We have a mutual follower, now we need to find out which user had the follower first as we want to use this date in the sorting.
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


        //Now we have found the mutual followers, in order traverse the sorted mutual tree to get the followers out in most recent first order
        //We will then convert from Nodes in the tree to an array as required in the interface.
        sortedMutualTree.clearNodes();
        sortedMutualTree.inOrderTraversal(sortedMutualTree.getRoot());
        MyArrayList<Node<Date, Integer>> sortedMutualList = sortedMutualTree.getNodesTraversed();
        int[] toReturn = new int[sortedMutualList.size()];
        for(int l=0; l<sortedMutualList.size(); l++) {
            toReturn[l] = sortedMutualList.get(l).getValue();
        }
        return toReturn;
    }

    /**
     * getMutualFollows() - O(n^2) method for finding common elements between two arraylists of follows.
     * @param uid1 - User 1 we want the follows for
     * @param uid2 - User 2 we want the follows for
     * @return - An integer array of user id's where the users are followed by both uid1 and uid2
     */
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
                    //We have found a mutual follows, we now need to find which user followed first and use this date in the sorted return tree.
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

        //Traverse the sorted follows tree so we can get the users in most recent first order
        //We will then convert from Nodes in the tree to an array of user ids and return it.
        sortedMutualTree.clearNodes();
        sortedMutualTree.inOrderTraversal(sortedMutualTree.getRoot());
        MyArrayList<Node<Date, Integer>> sortedMutualList = sortedMutualTree.getNodesTraversed();
        int[] toReturn = new int[sortedMutualList.size()];
        for(int l=0; l<sortedMutualList.size(); l++) {
            toReturn[l] = sortedMutualList.get(l).getValue();
        }
        return toReturn;
    }

    /**
     * getTopUsers() - O(n) method to get the tops users from the follower ranking arraylist. We need to sort this arraylist using a tree and the FollowerRanking Comparable
     * to get the users in the order required in the javadoc. I update the follower leaderboard when adding a follower relationship because it allows this method to run quicker
     * As specified in the addFollower method you could take that part of the method out and add it to a job queue so the method isnt slower than needs be.
     * @return An array of user ids where the first elements are the ones who have the most followers, and they got them before the following elements as required.
     */
    public int[] getTopUsers() {
        //Create a tree to sort the data within.
        AVLTree<FollowerRanking, Integer> sortedLeaderboardTree = new AVLTree<>();

        //Collect all of the users from the followerLeaderboard list and themm to a tree for sorting.
        for(int i=0; i<this.followerLeaderboard.size(); i++) {
            FollowerRanking ranking = this.followerLeaderboard.get(i);
            sortedLeaderboardTree.insertKeyValuePair(ranking, ranking.getUserId());
        }

        //Now we have all of our data in a tree, we should be able to inorder traverse this tree and get the correct leaderboard based on the FollowerRanking comparator.
        //Lets do that and then return the array of user ids rather than the nodes we have traversed.
        sortedLeaderboardTree.clearNodes();
        sortedLeaderboardTree.inOrderTraversal(sortedLeaderboardTree.getRoot());
        MyArrayList<Node<FollowerRanking, Integer>> sortedList = sortedLeaderboardTree.getNodesTraversed();

        int[] toReturn = new int[sortedList.size()];
        for(int i=0; i<sortedList.size(); i++) {
            toReturn[i] = sortedList.get(i).getKey().getUserId();
        }

        return toReturn;
    }



    //A class to represent a user in the follower ranking leaderboard. As the requirements for which user should come first in the leaderboard are somewhat complicated
    //I have made this class and implemented a comparator to it so we can compare which ranking entry should come first (followers and then date order if they have the same followers)
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
            if(this.whenUpdated == null || dateUpdated.after(this.whenUpdated)) {
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
        //The user with the higher followers comes first, if they have the same followers then the person who got that number of followers first wins.
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
//End of the followerstore class.
}
