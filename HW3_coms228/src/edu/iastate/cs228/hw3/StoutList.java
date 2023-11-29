package edu.iastate.cs228.hw3;

import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Implementation of the list interface based on linked nodes
 * that store multiple items per node.  Rules for adding and removing
 * elements ensure that each node (except possibly the last one)
 * is at least half full.
 */
public class StoutList<E extends Comparable<? super E>> extends AbstractSequentialList<E>
{
  /**
   * Default number of elements that may be stored in each node.
   */
  private static final int DEFAULT_NODESIZE = 4;
  
  /**
   * Number of elements that can be stored in each node.
  */
  private final int nodeSize;
  
  /**
   * Dummy node for head.  It should be private but set to public here only  
   * for grading purpose.  In practice, you should always make the head of a 
   * linked list a private instance variable.  
   */
  public Node head;
  
  /**
   * Dummy node for tail.
   */
  private Node tail;
  
  /**
   * Number of elements in the list.
   */
  private int size;
  
  /**
   * Constructs an empty list with the default node size.
   */
  public StoutList()
  {
    this(DEFAULT_NODESIZE);
  }

  /**
   * Constructs an empty list with the given node size.
   * @param nodeSize number of elements that may be stored in each node, must be 
   *   an even number
   */
  public StoutList(int nodeSize)
  {
    if (nodeSize <= 0 || nodeSize % 2 != 0)
    	throw new IllegalArgumentException();
    
    // Initialize dummy nodes
    head = new Node();
    tail = new Node();
    head.next = tail;
    tail.previous = head;
    this.nodeSize = nodeSize;
  }
  
  /**
   * Constructor for grading only.  Fully implemented. 
   * @param head
   * @param tail
   * @param nodeSize
   * @param size
   */
  public StoutList(Node head, Node tail, int nodeSize, int size)
  {
	  this.head = head; 
	  this.tail = tail; 
	  this.nodeSize = nodeSize; 
	  this.size = size; 
  }

  /*
   * returns the size (number of elements) of the list
   * 
   * @return the number of elements in this list
   */
  @Override
  public int size()
  {
    return size;
  }
  
  @Override
  public boolean add(E item)
  {
	// Check if the item to be added is null, and if so, throw an exception
    if(item == null) {
    	throw new NullPointerException();
    }
    
    // Check if the list already contains the item, and if so, return false
    if (contains(item)) {
    	return false;
    }
    
    // If the list is empty, create a new node and add the item to it
    if (size == 0) {
    	Node node = new Node();
    	node.addItem(item);
    	head.next = node;
    	node.previous = head;
    	node.next = tail;
    	tail.previous = node;
    } else {
    	// If the last node in the list has space, add the item to it
    	if (tail.previous.count < nodeSize) {
    		tail.previous.addItem(item);
    	} else {
    		// If the last node is full, create a new node and add the item to it
    		Node node = new Node();
    		node.addItem(item);
			Node temp = tail.previous;
			temp.next = node;
			node.previous = temp;
			node.next = tail;
			tail.previous = node;
    	}
    }
    
    size++; // Increment the size of the list
    return true; // Return true indicating the item was successfully added
    
  }
  
  @Override
  public void add(int pos, E item)
  {
	// Check if the position is out of bounds, and if so, throw an exception
    if(pos < 0 || pos > size)
    	throw new IndexOutOfBoundsException();
    
    // If the list is empty, simply add the item to the list
    if (head.next == tail)
    	add(item);
    
    // Find the node and offset corresponding to the given position
    NodeInfo node = find(pos);
    Node temp = node.node;
    int offset = node.off;
    
    // If the offset is 0, it means we're adding at the beginning of a node
    if(offset == 0) {
    	// If the previous node has space and it's not the head, add the item to the previous node
    	if (temp.previous.count < nodeSize && temp.previous != head) {
    		temp.previous.addItem(item);
    		size++;
    		return;
    	} else if (temp == tail) { // If the current node is the tail, simply add the item to the list
    		add(item);
    		size++;
    		return;
    	}
    }
    
    // If the current node has space, add the item to the current node at the specified offset
    if (temp.count < nodeSize) {
    	temp.addItem(offset, item);
    } else {
    	// If the current node is full, we need to split it and create a new successor node
    	Node newSuccessor = new Node();
    	int half = nodeSize / 2;
    	int count = 0;
    	// Move half of the items from the current node to the new successor node
    	while (count < half) {
    		newSuccessor.addItem(temp.data[half]);
    		temp.removeItem(half);
    		count++;
    	}
    	
    	// Update the links to insert the new successor node after the current node
    	Node oldSuccessor = temp.next;
    	
    	temp.next = newSuccessor;
    	newSuccessor.previous = temp;
    	newSuccessor.next = oldSuccessor;
    	oldSuccessor.previous = newSuccessor;
    	
    	// Add the item to the appropriate node based on the offset
    	if (offset <= nodeSize / 2) {
    		temp.addItem(offset, item);
    	}
    	
    	if (offset > nodeSize / 2) {
    		newSuccessor.addItem((offset - nodeSize / 2), item);
    	}
    	
    }
    
    size++; // Increment the size of the list
  }

  @Override
  public E remove(int pos)
  {
	  	// Check if the position is out of bounds, and if so, throw an exception
		if (pos < 0 || pos > size)
			throw new IndexOutOfBoundsException();
		
		// Find the node and offset corresponding to the given position
		NodeInfo nodeInfo = find(pos);
		Node temp = nodeInfo.node;
		int offset = nodeInfo.off;
		E nodeValue = temp.data[offset]; // Store the value to be removed for returning later

		// If the node is the last one and contains only one element
		if (temp.next == tail && temp.count == 1) {
			Node predecessor = temp.previous;
			// Remove the node by updating the links of its predecessor and successor
			predecessor.next = temp.next;
			temp.next.previous = predecessor;
			temp = null;
		} else if (temp.next == tail || temp.count > nodeSize / 2) {
			// If the node is the last one or contains more than half of the nodeSize elements, simply remove the element at the offset
			temp.removeItem(offset);
		} else {
			// If the node contains half or less of the nodeSize elements
			temp.removeItem(offset);
			Node succesor = temp.next;
			
			// If the successor node contains more than half of the nodeSize elements
			if (succesor.count > nodeSize / 2) {
				// Move the first element from the successor to the current node
				temp.addItem(succesor.data[0]);
				succesor.removeItem(0);
			} else if (succesor.count <= nodeSize / 2) {
				// If the successor node contains half or less of the nodeSize elements
	            // Move all elements from the successor to the current node
				for (int i = 0; i < succesor.count; i++) {
					temp.addItem(succesor.data[i]);
				}
				// Remove the successor node by updating the links
				temp.next = succesor.next;
				succesor.next.previous = temp;
				succesor = null;
			}
		}
		size--; // Decrement the size of the list
		return nodeValue; // Return the removed value
  }

  /**
   * Sort all elements in the stout list in the NON-DECREASING order. You may do the following. 
   * Traverse the list and copy its elements into an array, deleting every visited node along 
   * the way.  Then, sort the array by calling the insertionSort() method.  (Note that sorting 
   * efficiency is not a concern for this project.)  Finally, copy all elements from the array 
   * back to the stout list, creating new nodes for storage. After sorting, all nodes but 
   * (possibly) the last one must be full of elements.  
   *  
   * Comparator<E> must have been implemented for calling insertionSort().    
   */
  public void sort()
  {
	  	// Create an array to hold all the data elements from the list
		E[] sortDataList = (E[]) new Comparable[size];

		int tempIndex = 0;
		Node temp = head.next;
		
		// Traverse through the list and copy all elements to the array
		while (temp != tail) {
			for (int i = 0; i < temp.count; i++) {
				sortDataList[tempIndex] = temp.data[i];
				tempIndex++;
			}
			temp = temp.next;
		}

		// Reset the list to be empty
		head.next = tail;
		tail.previous = head;

		// Sort the array using insertion sort in non-decreasing order
		insertionSort(sortDataList, new ElementComparator());
		size = 0;
		
		// Copy sorted elements back to the list
		for (int i = 0; i < sortDataList.length; i++) {
			add(sortDataList[i]);
		} 
  }
  
  /**
   * Sort all elements in the stout list in the NON-INCREASING order. Call the bubbleSort()
   * method.  After sorting, all but (possibly) the last nodes must be filled with elements.  
   *  
   * Comparable<? super E> must be implemented for calling bubbleSort(). 
   */
  public void sortReverse() 
  {
	  // Create an array to hold all the data elements from the list
	  E[] reverseSortDataList = (E[]) new Comparable[size];
	  
	  int tempIndex = 0;
	  Node tempNode = head.next;
	  
	  // Traverse through the list and copy all elements to the array
	  while (tempNode != tail) {
		  for(int i = 0; i < tempNode.count; i++) {
			  reverseSortDataList[tempIndex] = tempNode.data[i];
			  tempIndex++;
		  }
		  tempNode = tempNode.next;
	  }
	  
	  // Reset the list to be empty
	  head.next = tail;
	  tail.previous = head;
	  
	  // Sort the array using bubble sort in non-increasing order
	  bubbleSort(reverseSortDataList);
	  size = 0;
	  for (int i = 0; i < reverseSortDataList.length; i++) {
		  add(reverseSortDataList[i]);
	  }
  }
  
  @Override
  public Iterator<E> iterator()
  {
	  return new StoutListIterator(); 
  }

  @Override
  public ListIterator<E> listIterator()
  {
	  return new StoutListIterator(); 
  }

  @Override
  public ListIterator<E> listIterator(int index)
  {
	  return new StoutListIterator(index); 
  }
  
  /**
   * 
   * Checks if the StoutList contains the specified item.
   * 
   * This method traverses the nodes of the StoutList, checking each element
   * in the nodes' data arrays. If it finds an element that matches the 
   * specified item, it returns true. Otherwise, it returns false.
   * 
   * @param item the element whose presence in this list is to be tested
   * @return true if this list contains the specified element, false otherwise
   */
  public boolean contains(E item) {
	  if (size < 1) 
		  return false;
	  
	  Node temp = head.next;  
	  while(temp != tail) {
		  for(int i = 0; i < temp.count; i++) {
			  if(temp.data[i].equals(item))
				  return true;
			  temp = temp.next;
		  }
	  }
	  return false;
  }
  
  /**
   * Returns a string representation of this list showing
   * the internal structure of the nodes.
   * 
   * @return a string representation of the list
   */
  public String toStringInternal()
  {
    return toStringInternal(null);
  }

  /**
   * Returns a string representation of this list showing the internal
   * structure of the nodes and the position of the iterator.
   *
   * @param iter an iterator for this list
   * @return a string representation of the list
   */
  public String toStringInternal(ListIterator<E> iter) 
  {
      int count = 0;
      int position = -1;
      if (iter != null) {
          position = iter.nextIndex();
      }

      StringBuilder sb = new StringBuilder();
      sb.append('[');
      Node current = head.next;
      while (current != tail) {
          sb.append('(');
          E data = current.data[0];
          if (data == null) {
              sb.append("-");
          } else {
              if (position == count) {
                  sb.append("| ");
                  position = -1;
              }
              sb.append(data.toString());
              ++count;
          }

          for (int i = 1; i < nodeSize; ++i) {
             sb.append(", ");
              data = current.data[i];
              if (data == null) {
                  sb.append("-");
              } else {
                  if (position == count) {
                      sb.append("| ");
                      position = -1;
                  }
                  sb.append(data.toString());
                  ++count;

                  // iterator at end
                  if (position == size && count == size) {
                      sb.append(" |");
                      position = -1;
                  }
             }
          }
          sb.append(')');
          current = current.next;
          if (current != tail)
              sb.append(", ");
      }
      sb.append("]");
      return sb.toString();
  }
  
  /**
   * Node type for this list.  Each node holds a maximum
   * of nodeSize elements in an array.  Empty slots
   * are null.
   */
  private class Node
  {
    /**
     * Array of actual data elements.
     */
    // Unchecked warning unavoidable.
    public E[] data = (E[]) new Comparable[nodeSize];
    
    /**
     * Link to next node.
     */
    public Node next;
    
    /**
     * Link to previous node;
     */
    public Node previous;
    
    /**
     * Index of the next available offset in this node, also 
     * equal to the number of elements in this node.
     */
    public int count;

    /**
     * Adds an item to this node at the first available offset.
     * Precondition: count < nodeSize
     * @param item element to be added
     */
    void addItem(E item)
    {
      if (count >= nodeSize)
      {
        return;
      }
      data[count++] = item;
      //useful for debugging
      //      System.out.println("Added " + item.toString() + " at index " + count + " to node "  + Arrays.toString(data));
    }
  
    /**
     * Adds an item to this node at the indicated offset, shifting
     * elements to the right as necessary.
     * 
     * Precondition: count < nodeSize
     * @param offset array index at which to put the new element
     * @param item element to be added
     */
    void addItem(int offset, E item)
    {
      if (count >= nodeSize)
      {
    	  return; // Node is full, so exit without adding
      }
      // Shift elements to the right to make space for the new item
      for (int i = count - 1; i >= offset; --i)
      {
        data[i + 1] = data[i];
      }
      ++count;
      data[offset] = item;
      //useful for debugging 
      //System.out.println("Added " + item.toString() + " at index " + offset + " to node: "  + Arrays.toString(data));
    }

    /**
     * Deletes an element from this node at the indicated offset, 
     * shifting elements left as necessary.
     * Precondition: 0 <= offset < count
     * @param offset
     */
    void removeItem(int offset)
    {
      E item = data[offset];
      // Shift elements to the left to fill the gap left by the removed item
      for (int i = offset + 1; i < nodeSize; ++i)
      {
        data[i - 1] = data[i];
      }
      data[count - 1] = null; // Clear the last slot
      --count; // Decrement the count of elements in this node
    }    
  }

  /**
   * An iterator for the StoutList that allows forward and backward traversal, 
   * as well as modification of the list during iteration.
   */
  private class StoutListIterator implements ListIterator<E>
  {
	/**
	 * Constants to represent the last action performed by the iterator
	 */
	final int PREVIOUS = 0;
	final int NEXT = 1;
	
	/**
	 * Current position of the iterator
	 */
	int currentPosition;
	
	/**
	 * Array to store the list's data for easier iteration
	 */
	public E[] dataList;
	
	/**
	 * Keeps track of the last action performed (NEXT, PREVIOUS, or -1 if none)
	 */
	int lastAction;
	  
	 /**
     * Sets up the dataList array with the current elements of the list.
     */
	private void setup() {
		dataList = (E[]) new Comparable[size];
		
		int tempIndex = 0;
		Node temp = head.next;
		while (temp != tail) {
			for (int i = 0; i < temp.count; i++) {
				dataList[tempIndex] = temp.data[i];
				tempIndex++;
			}
			temp = temp.next;
		}
	}
	
	/**
     * Default constructor initializes the iterator to the start of the list.
     */
    public StoutListIterator()
    {
    	currentPosition = 0;
    	lastAction = -1;
    	setup();
    }

    /**
     * Constructor finds node at a given position.
     * @param pos
     */
    public StoutListIterator(int pos)
    {
    	currentPosition = pos;
    	lastAction = -1;
    	setup();
    }

    @Override
    public boolean hasNext()
    {
    	if(currentPosition >= size) {
    		return false;
    	} else {
    		return true;
    	}
    }

    @Override
    public E next()
    {
    	if (!hasNext())
    		throw new NoSuchElementException();
    	lastAction = NEXT;
    	return dataList[currentPosition++];
    }

    @Override
    public void remove()
    {
    	if (lastAction == NEXT) {
    		StoutList.this.remove(currentPosition - 1);
    		setup();
    		lastAction = -1;
    		currentPosition--;
    		if(currentPosition < 0)
    			currentPosition = 0;
    		} else if (lastAction == PREVIOUS) {
    			StoutList.this.remove(currentPosition - 1);
        		setup();
        		lastAction = -1;
    		} else {
    			throw new IllegalStateException();
    		}
    }
    
    @Override
    public boolean hasPrevious() {
    	if (currentPosition <= 0) {
    		return false;
    	} else {
    		return true;
    	}
    }
    
    @Override
    public int nextIndex() {
    	return currentPosition;
    }
    
    @Override
    public E previous() {
    	if (!hasPrevious())
    		throw new NoSuchElementException();
    	lastAction = PREVIOUS;
    	currentPosition--;
    	return dataList[currentPosition];
    }
    
    @Override
    public int previousIndex() {
    	return currentPosition - 1;
    }
    
    @Override
    public void set(E o1) {
		if (lastAction == NEXT) {
			NodeInfo nodeInfo = find(currentPosition - 1);
			nodeInfo.node.data[nodeInfo.off] = o1;
			dataList[currentPosition - 1] = o1;
		} else if (lastAction == PREVIOUS) {
			NodeInfo nodeInfo = find(currentPosition);
			nodeInfo.node.data[nodeInfo.off] = o1;
			dataList[currentPosition] = o1;
		} else {
			throw new IllegalStateException();
		}
    }
    
    @Override
    public void add(E o1) {
    	if (o1 == null)
    		throw new NullPointerException();
    	StoutList.this.add(currentPosition, o1);
    	currentPosition++;
    	setup();
    	lastAction = -1;
    }
  
  }
  

  /**
   * Sort an array arr[] using the insertion sort algorithm in the NON-DECREASING order. 
   * @param arr   array storing elements from the list 
   * @param comp  comparator used in sorting 
   */
  private void insertionSort(E[] arr, Comparator<? super E> comp)
  {
	  for (int i = 0; i < arr.length; i++) {
		  E key = arr[i];
		  int j = i - 1;
		  
		  while (j >= 0 && comp.compare(arr[j], key) > 0) {
			  arr[j + 1] = arr[j];
			  j--;
		  }
		  arr[j + 1] = key;
	  }
  }
  
  /**
   * Sort arr[] using the bubble sort algorithm in the NON-INCREASING order. For a 
   * description of bubble sort please refer to Section 6.1 in the project description. 
   * You must use the compareTo() method from an implementation of the Comparable 
   * interface by the class E or ? super E. 
   * @param arr  array holding elements from the list
   */
  private void bubbleSort(E[] arr)
  {
	  int n = arr.length;
	  for (int i = 0; i < n - 1; i++) {
		  for (int j = 0; j < n - i - 1; j++) {
			  if(arr[j].compareTo(arr[j + 1]) < 0) {
				  E temp = arr[j];
				  arr[j] = arr[j + 1];
				  arr[j + 1] = temp;
			  }
		  }
	  }
  }
  
  /**
   * Represents information about a node in the StoutList.
   * This class is used to encapsulate a node and its offset position 
   * within the node, which is helpful during various list operations.
   */
  private class NodeInfo{
	  
	  public Node node;
	  
	  public int off;
	  
	  public NodeInfo(Node node, int off) {
		  this.node = node;
		  this.off = off;
	  }
  }
  
  /**
   * Finds the node and its offset for a given position in the list.
   * 
   * @param pos the position in the list
   * @return a NodeInfo object encapsulating the node and its offset for the given position
   */
  private NodeInfo find(int pos) {
	  // Start from the first node after the head
	  Node temp = head.next;
	  
	  // Current position in the list
	  int currPos = 0;
	  
	  // Traverse the list until we reach the tail
	  while (temp != tail) {
		  // If the current position plus the count of elements in the current node is less than or equal to the target position
		  if (currPos + temp.count <= pos) {
			  // Move to the next node
			  currPos += temp.count;
			  temp = temp.next;
			  continue;
		  }
		  // If we've found the node containing the target position, create a NodeInfo object with the node and its offset
		  NodeInfo node = new NodeInfo(temp, pos - currPos);
		  return node;		  
	  }
	  // If the position is not found in the list, return null
	  return null;
  }
  
  /**
   * A comparator for elements that implement the Comparable interface.
   * This comparator is used to compare two elements based on their natural ordering.
   *
   * @param <E> the type of elements to be compared, which must implement the Comparable interface
   */
  class ElementComparator<E extends Comparable<E>> implements Comparator<E>{
	  
    /**
    * Compares two elements based on their natural ordering.
    * 
    * @param o1 the first element to be compared
    * @param o2 the second element to be compared
    * @return a negative integer, zero, or a positive integer as the first element
	*         is less than, equal to, or greater than the second element
	*/
	@Override
	public int compare(E o1, E o2) {
		// Use the compareTo method of the Comparable interface to compare the two elements
		return o1.compareTo(o2);
	}
	  
  }
  
  /**
   * main method to debug and test different scenarios.
   */
//  public static void main (String [] args) {
	  
//	  StoutList list = new StoutList();
//
//	  list.add("A");
//	  list.add("B");
//	  list.add("C");
//	  list.add("D");
//	  list.add("C");
//	  list.add("D");
//	  list.add("E");
//	  list.remove(2);
//	  list.remove(2);
//	  System.out.println("(Figure 3) Starting list:\n" +list.toStringInternal());
//	  System.out.println();
//
//	  list.add("V");
//	  System.out.println("(Figure 4) After adding V:\n" +list.toStringInternal());
//	  System.out.println();
//
//	  list.add("W");
//	  System.out.println("(Figure 5) After adding W:\n" +list.toStringInternal());
//	  System.out.println();
//
//	  list.add(2, "X");
//	  System.out.println("(Figure 6) After adding X at 2:\n" +list.toStringInternal());
//	  System.out.println();
//
//	  list.add(2, "Y");
//	  System.out.println("(Figure 7) After adding Y at 2:\n" +list.toStringInternal());
//	  System.out.println();
//
//	  list.add(2, "Z");
//	  System.out.println("(Figure 8) After adding Z at 2:\n" +list.toStringInternal());
//	  System.out.println();
//
//	  list.remove(9);
//	  System.out.println("(Figure 10) After removing W at 9:\n" +list.toStringInternal());
//	  System.out.println();
//
//	  list.remove(3);
//	  System.out.println("(Figure 11) After removing Y at 3:\n" +list.toStringInternal());
//	  System.out.println();
//
//	  list.remove(3);
//	  System.out.println("(Figure 12) After removing X at 3:\n" +list.toStringInternal());
//	  System.out.println();
//
//	  list.remove(5);
//	  System.out.println("(Figure 13) After removing E at 5:\n" +list.toStringInternal());
//	  System.out.println();
//
//	  list.remove(3);
//	  System.out.println("(Figure 14) After removing C at 3:\n" +list.toStringInternal());
//	  System.out.println();
	  
//	    StoutList<Integer> list = new StoutList<>();
//
//	    // 1. Basic Operations
//	    System.out.println("=== Basic Operations ===");
//	    list.add(5);
//	    list.add(3);
//	    list.add(8);
//	    list.add(1);
//	    System.out.println("List after adding 5, 3, 8, 1: " + list.toStringInternal());
//	    list.remove(1);
//	    System.out.println("List after removing index 1: " + list.toStringInternal());
//	    System.out.println("Size of list: " + list.size());
//
////	    // 2. Boundary Conditions
////	    System.out.println("\n=== Boundary Conditions ===");
////	    list.add(0, 2);
////	    System.out.println("List after adding 2 at index 0: " + list.toStringInternal());
////	    list.add(list.size(), 10);
////	    System.out.println("List after adding 10 at the end: " + list.toStringInternal());
////	    list.add(list.size() / 2, 6);
////	    System.out.println("List after adding 6 in the middle: " + list.toStringInternal());
//
////	    // 3. Sorting
////	    System.out.println("\n=== Sorting ===");
////	    list.add(7);
////	    list.add(1);
////	    list.add(9);
////	    list.add(3);
////	    System.out.println("List before sorting: " + list.toStringInternal());
////	    list.sort();
////	    System.out.println("List after sorting: " + list.toStringInternal());
////
////	    // 4. Reverse Sorting
////	    System.out.println("\n=== Reverse Sorting ===");
////	    list.sortReverse();
////	    System.out.println("List after reverse sorting: " + list.toStringInternal());
//
////	    // 5. Iterator
////	    System.out.println("\n=== Iterator ===");
////	    ListIterator<Integer> iterator = list.listIterator();
////	    while (iterator.hasNext()) {
////	        System.out.print(iterator.next() + " ");
////	    }
////	    System.out.println("\nUsing iterator to modify list...");
////	    iterator.previous();
////	    iterator.set(99);  // Change the last element to 99
////	    System.out.println("List after modification: " + list.toStringInternal());
//
////	    //6. Exceptions
////	    
////	    System.out.println("\n=== Exceptions ===");
////	    try {
////	        list.add(null);
////	    } catch (Exception e) {
////	        System.out.println("Exception when adding null: " + e.getMessage());
////	    }
////	    try {
////	        list.remove(list.size() + 1);
////	    } catch (Exception e) {
////	        System.out.println("Exception when removing from invalid index: " + e.getMessage());
////	    }
//	    
//	  
//  }
}