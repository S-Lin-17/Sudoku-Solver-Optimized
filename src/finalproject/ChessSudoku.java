package finalproject;

import java.util.*;
import java.io.*;


public class ChessSudoku
{
	/* SIZE is the size parameter of the Sudoku puzzle, and N is the square of the size.  For
	 * a standard Sudoku puzzle, SIZE is 3 and N is 9.
	 */
	public int SIZE, N;

	/* The grid contains all the numbers in the Sudoku puzzle.  Numbers which have
	 * not yet been revealed are stored as 0.
	 */
	public int grid[][];

	/* Booleans indicating whether of not one or more of the chess rules should be
	 * applied to this Sudoku.
	 */
	public boolean knightRule;
	public boolean kingRule;
	public boolean queenRule;


	// Field that stores the same Sudoku puzzle solved in all possible ways
	public HashSet<ChessSudoku> solutions = new HashSet<ChessSudoku>();

	// Private helper method that has creates all possible values for all squares of a sudoku
	private int[][][] createPossibleValues (int size){

		int[][][] possibleValues = new int[size][size][size];

		for (int i = 0; i < size; i++){
			for (int j = 0; j < size; j++){
				for (int k = 0; k < size; k++){
					possibleValues[i][j][k] = k+1;
				}
			}
		}
		return possibleValues;
	}

	// Private field that holds the possibleValues 3D array
	private int[][][] possibleValues;

	// Private getter helper method that retrieves the grid 2D array
	private int[][] getGrid(){
		return this.grid;
	}

	// Private copy constructor for multiple solutions
	private ChessSudoku(int size, int[][] newGrid){

		SIZE = size;
		N = size*size;

		this.grid = new int[N][N];

		for (int i = 0; i < this.N; i++){
			for (int j = 0; j < this.N; j++){
				this.grid[i][j] = newGrid[i][j];
			}
		}
	}

	// Private helper method that determines whether the number is legal or not: returns true if there's a conflict (TODO: add allowedvalues arrays for each number to make computation time faster)
	private boolean hasConflict(int row, int column, int digit){

		if (!((0 <= row && row <= this.N-1) && (0 <= column && column <= this.N-1))){
			System.out.println("Row: " + row + "| Column: " + column);
			throw new IllegalArgumentException("Row and/or column should not be used here. Check code.");
		}

		// Same square conflict (checking if the digit appears anywhere else in the same "square" / box)
		int rowSquare = row - (row % this.SIZE);
		int columnSquare = column - (column % this.SIZE);

		for (int new_r = rowSquare; new_r < rowSquare + this.SIZE; new_r++){
			for (int new_c = columnSquare; new_c < columnSquare + this.SIZE; new_c++){
				if (this.grid[new_r][new_c] == digit){
					return true;
				}
			}
		}

		// Column conflict (checking if the digit appears anywhere else in the same column)
		for (int new_r = 0; new_r < this.N; new_r++){

			if (this.grid[new_r][column] == digit){
				return true;
			}
		}

		// Row conflict (checking if the digit appears anywhere else in the same row)
		for (int new_c = 0; new_c < this.N; new_c++){

			if (this.grid[row][new_c] == digit){
				return true;
			}
		}

		// Knight rule conflict (only "activated" if it is set to true)
		if (this.knightRule){

			// Going case by case (from top to bottom, left to right): maximum of 8 Knight moves no matter the puzzle size
			if (row - 2 >= 0 && column - 1 >= 0){
				if (this.grid[row-2][column-1] == digit){
					return true;
				}
			}

			if (row - 2 >= 0 && column + 1 < this.N){
				if (this.grid[row-2][column+1] == digit){
					return true;
				}
			}

			if (row - 1 >= 0 && column - 2 >= 0){
				if (this.grid[row-1][column-2] == digit){
					return true;
				}
			}

			if (row - 1 >= 0 && column + 2 < this.N){
				if (this.grid[row-1][column+2] == digit){
					return true;
				}
			}

			if (row + 1 < this.N && column - 2 >= 0){
				if (this.grid[row+1][column-2] == digit){
					return true;
				}
			}

			if (row + 1 < this.N && column + 2 < this.N){
				if (this.grid[row+1][column+2] == digit){
					return true;
				}
			}

			if (row + 2 < this.N && column - 1 >= 0){
				if (this.grid[row+2][column-1] == digit){
					return true;
				}
			}

			if (row + 2 < this.N && column + 1 < this.N){
				if (this.grid[row+2][column+1] == digit){
					return true;
				}
			}
		}

		// King rule conflict (only "activated" if it is set to true)
		if (this.kingRule){

			// Also going case by case since there is a maximum of 4 possible moves (excluding classical Sudoku vertical and horiz constraints)

			// Top-left diagonal
			if (row - 1 >= 0 && column - 1 >= 0){
				if (this.grid[row-1][column-1] == digit){
					return true;
				}
			}

			// Top-right diagonal
			if (row - 1 >= 0 && column + 1 < this.N){
				if (this.grid[row-1][column+1] == digit){
					return true;
				}
			}

			// Bottom-left diagonal
			if (row + 1 < this.N && column - 1 >= 0){
				if (this.grid[row+1][column-1] == digit){
					return true;
				}
			}

			// Bottom-right diagonal
			if (row + 1 < this.N && column + 1 < this.N){
				if (this.grid[row+1][column+1] == digit){
					return true;
				}
			}
		}

		// Queen rule conflict (only "activated" if it is set to true and said digit is the largest one)
		if (this.queenRule && digit == this.N){

			// Here, only check for diagonals since row, column and (SIZE X SIZE) box are checked before anyway
			int topRowStart, topColumnStart;
			int bottomRowStart, bottomColumnStart;

			// Top-right half of the grid
			if (row < column){
				topRowStart = 0;
				topColumnStart = column - row;

				bottomRowStart = (column + row < this.N) ? (column + row): (this.N - 1);
				bottomColumnStart = (column + row < this.N) ? (0) : (column + row - (this.N - 1));
			}
			else if (column < row){
				topRowStart = row - column;
				topColumnStart = 0;

				bottomRowStart = (column + row < this.N) ? (column+row) : (this.N - 1);
				bottomColumnStart = (column + row < this.N) ? (0) : (column + row - (this.N - 1));
			}

			// If the digit is on the "main" diagonal (top-bottom + left-right)
			else{
				topRowStart = 0;
				topColumnStart = 0;

				bottomRowStart = (column + row < this.N) ? (column+row) : (this.N - 1);
				bottomColumnStart = (column + row < this.N) ? (0) : (column+ row - (this.N - 1));
			}

//			if ((bottomRowStart == -1 || bottomColumnStart == -1)){
//				System.out.println("Mistake at this place: check row " + row + " and column " + column);
//				System.out.println("bottomRowStart = " + bottomRowStart + "| bottomColumnStart = " + bottomColumnStart);
//			}

			// Top to bottom, left to right diagonal
			for (int r = topRowStart, c = topColumnStart; r < this.N && c < this.N; r++, c++){
				if (this.grid[r][c] == digit && (r != row && c != column)){
					return true;
				}
			}

			// Bottom to top, left to right diagonal
			for (int r = bottomRowStart, c = bottomColumnStart; r >= 0 && c < this.N; r--, c++){
				if (this.grid[r][c] == digit && (r != row && c != column)){
					return true;
				}
			}
		}

		// If all sudoku rules apply, no conflicts = return false
		return false;
	}

	// Private helper method called on *this* ChessSudoku that helps for the recursion
	private boolean recursiveSolve(int row, int column, boolean allSolutions) {

		// Allowed values array list
//		List<Integer> allowedValues = new ArrayList<>();
//		for (int i = 0; i < this.N; i++){
//			allowedValues.add(i+1);
//		}

		// Iterating through Sudoku rows and columns (no for loops to save time)
		if (column >= this.N){
			column = 0;
			row++;

			// This would be the base case of the recursive method (row and column reach end of grid: all cells are explored)
			if ((row >= this.N && column == 0) && !allSolutions){
				return true;
			}

			else if ((row >= this.N && column == 0) && allSolutions){
				ChessSudoku solvedPuzzle = new ChessSudoku(this.SIZE, this.grid);
				this.solutions.add(solvedPuzzle);
				return false;
			}
		}

//		for (int digit = allowedValues.get(0); digit <= allowedValues.size(); digit++){

		for (int digit: possibleValues[row][column]){

//			System.out.println("Row: " + row + "| Column: " + column + "| Digit: " + digit);
			if (this.grid[row][column] == 0){

				if (!hasConflict(row, column, digit)){
					this.grid[row][column] = digit;

					if (recursiveSolve(row, column + 1, allSolutions)){
						return true;
					}
					else{
						this.grid[row][column] = 0;
					}
				}
//				else{
//				    int[] n = new int[1];
//					possibleValues[row][column] = this.grid[row][column];
//				}
			}

			else{
//				allowedValues.clear();
				return recursiveSolve(row, column + 1, allSolutions);
			}
		}

		return false;
	}

	/* The solve() method should remove all the unknown characters ('x') in the grid
	 * and replace them with the numbers in the correct range that satisfy the constraints
	 * of the Sudoku puzzle. If true is provided as input, the method should find finds ALL
	 * possible solutions and store them in the field named solutions. */
	public void solve(boolean allSolutions) {

		int[][] unsolvedGrid = this.grid;	// Used when no solution is found (in allSolutions = false)

		// Local field for iterating through puzzle
		int currentRow = 0;
		int currentColumn = 0;

		possibleValues = createPossibleValues(this.N);

		if (allSolutions){

			recursiveSolve(currentRow, currentColumn, allSolutions);		// Assuming that there is at least one solution found anyway

//			int i = 1;

			for (ChessSudoku e: this.solutions){
//				System.out.println("Solution " + i + ":");
//				e.print();
				this.grid = e.grid;
//				i++;
				break;
			}
		}

		else{

			if (recursiveSolve(currentRow, currentColumn, allSolutions));
			else {
				this.grid = unsolvedGrid;
			}
		}
	}

	/*****************************************************************************/
	/* NOTE: YOU SHOULD NOT HAVE TO MODIFY ANY OF THE METHODS BELOW THIS LINE. */
	/*****************************************************************************/

	/* Default constructor.  This will initialize all positions to the default 0
	 * value.  Use the read() function to load the Sudoku puzzle from a file or
	 * the standard input. */
	public ChessSudoku( int size ) {
		SIZE = size;
		N = size*size;

		grid = new int[N][N];
		for( int i = 0; i < N; i++ )
			for( int j = 0; j < N; j++ )
				grid[i][j] = 0;
	}


	/* readInteger is a helper function for the reading of the input file.  It reads
	 * words until it finds one that represents an integer. For convenience, it will also
	 * recognize the string "x" as equivalent to "0". */
	static int readInteger( InputStream in ) throws Exception {
		int result = 0;
		boolean success = false;

		while( !success ) {
			String word = readWord( in );

			try {
				result = Integer.parseInt( word );
				success = true;
			} catch( Exception e ) {
				// Convert 'x' words into 0's
				if( word.compareTo("x") == 0 ) {
					result = 0;
					success = true;
				}
				// Ignore all other words that are not integers
			}
		}

		return result;
	}


	/* readWord is a helper function that reads a word separated by white space. */
	static String readWord( InputStream in ) throws Exception {
		StringBuffer result = new StringBuffer();
		int currentChar = in.read();
		String whiteSpace = " \t\r\n";
		// Ignore any leading white space
		while( whiteSpace.indexOf(currentChar) > -1 ) {
			currentChar = in.read();
		}

		// Read all characters until you reach white space
		while( whiteSpace.indexOf(currentChar) == -1 ) {
			result.append( (char) currentChar );
			currentChar = in.read();
		}
		return result.toString();
	}


	/* This function reads a Sudoku puzzle from the input stream in.  The Sudoku
	 * grid is filled in one row at at time, from left to right.  All non-valid
	 * characters are ignored by this function and may be used in the Sudoku file
	 * to increase its legibility. */
	public void read( InputStream in ) throws Exception {
		for( int i = 0; i < N; i++ ) {
			for( int j = 0; j < N; j++ ) {
				grid[i][j] = readInteger( in );
			}
		}
	}


	/* Helper function for the printing of Sudoku puzzle.  This function will print
	 * out text, preceded by enough ' ' characters to make sure that the printint out
	 * takes at least width characters.  */
	void printFixedWidth( String text, int width ) {
		for( int i = 0; i < width - text.length(); i++ )
			System.out.print( " " );
		System.out.print( text );
	}


	/* The print() function outputs the Sudoku grid to the standard output, using
	 * a bit of extra formatting to make the result clearly readable. */
	public void print() {
		// Compute the number of digits necessary to print out each number in the Sudoku puzzle
		int digits = (int) Math.floor(Math.log(N) / Math.log(10)) + 1;

		// Create a dashed line to separate the boxes
		int lineLength = (digits + 1) * N + 2 * SIZE - 3;
		StringBuffer line = new StringBuffer();
		for( int lineInit = 0; lineInit < lineLength; lineInit++ )
			line.append('-');

		// Go through the grid, printing out its values separated by spaces
		for( int i = 0; i < N; i++ ) {
			for( int j = 0; j < N; j++ ) {
				printFixedWidth( String.valueOf( grid[i][j] ), digits );
				// Print the vertical lines between boxes
				if( (j < N-1) && ((j+1) % SIZE == 0) )
					System.out.print( " |" );
				System.out.print( " " );
			}
			System.out.println();

			// Print the horizontal line between boxes
			if( (i < N-1) && ((i+1) % SIZE == 0) )
				System.out.println( line.toString() );
		}
	}


	/* The main function reads in a Sudoku puzzle from the standard input,
	 * unless a file name is provided as a run-time argument, in which case the
	 * Sudoku puzzle is loaded from that file.  It then solves the puzzle, and
	 * outputs the completed puzzle to the standard output.*/
	public static void main( String args[] ) throws Exception {
		InputStream in = new FileInputStream("medium3x3_eightSolutions.txt");

		// The first number in all Sudoku files must represent the size of the puzzle.  See
		// the example files for the file format.
		int puzzleSize = readInteger( in );
		if( puzzleSize > 100 || puzzleSize < 1 ) {
			System.out.println("Error: The Sudoku puzzle size must be between 1 and 100.");
			System.exit(-1);
		}

		ChessSudoku s = new ChessSudoku( puzzleSize );

		// You can modify these to add rules to your sudoku
		s.knightRule = false;
		s.kingRule = false;
		s.queenRule = false;

		// read the rest of the Sudoku puzzle
		s.read( in );

		System.out.println("Before the solve:");
		s.print();
		System.out.println();

		// Solve the puzzle by finding one solution.
		s.solve(true);

		// Print out the (hopefully completed!) puzzle
		System.out.println("After the solve:");
		s.print();
	}

}