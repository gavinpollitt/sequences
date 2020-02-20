package uk.gav;

import java.util.ArrayList;
import java.util.List;

// Working 3 12 25 42 63
// Working 0 6 16 30 48
// Working 2 6 12 20 30
// Working -2 -2 -2 -2 -2
// Working -11 -14 -9 10 49 114
// Working 6 10 16 24
public class Sequence {

	private final static boolean WORKING = true;
	public static void main(String args[]) {
		List<Integer> sequence = new ArrayList<>();
		if (args.length < 4) {
			throw new IllegalArgumentException("At least 4 arguments must be supplied");
		} else {
			for (int i = 0; i < args.length; i++) {
				sequence.add(Integer.parseInt(args[i]));
			}
		}

		Sequence s = new Sequence();

		try {
			System.out.println("Sequence to evaluate:" + sequence);
			System.out.println();
			List<Index> inds = s.evaluate(sequence);
			System.out.println("\n\nFull Formula:" + Index.getFormula(inds));
			System.out.println();

		} catch (Exception e) {
			System.out.println("The numbers provided do not obey an Arithmetic Sequence");
		}

	}

	private List<Index> evaluate(List<Integer> sequence) throws UnresolvableSequence {
		List<Index> out = fullPass(sequence);
		
		return out;
	}

	private List<Index> fullPass(final List<Integer> sequence) throws UnresolvableSequence {
		
		working("STAGE {");
		working("  Active Sequence-->" + sequence);
		int max = 1;

		DiffSeq ds = diffSeq(sequence);
		working("  Pass-->" + max);
		working("  Calculated difference Sequence -->" + ds.diffs);
		while (ds.dt != DIFF_TYPE.COMPLETE && ds.dt != DIFF_TYPE.ZEROS) {
			max++;
			ds = diffSeq(ds.diffs);
			working("  Pass-->" + max);
			working("  Calculated difference Sequence -->" + ds.diffs);
		}
		
		final Index index = new Index(ds.getDiff() / factorial(max), max);
		working("  Calculated Multiplier at line " + max + " (" + ds.getDiff() + "/" + max + "!)-->" + (ds.getDiff() / factorial(max)));
		working("  Calculated Power-->" + max);

		List<Integer> newSeq = new ArrayList<>(sequence.size());
		for (int i = 0; i < sequence.size(); i++) {
			newSeq.add(sequence.get(i) - ((index.negative?-1:1)*index.multiplier * (int) Math.pow(i + 1, index.power)));
		}

		List<Index> formPart = new ArrayList<>(1);
		formPart.add(index);

		
		if (max != 1) {
			working("  Calculated formula part-->" + Index.getFormula(formPart));
			working("  Calculated sequence for next Stage-->" + newSeq);
			working("}\n");
			formPart.addAll(evaluate(newSeq));
		}			
		else {
			Index ind = new Index(newSeq.get(0),0);
			working("  Calculated formula part-->" + Index.getFormula(formPart));
			working("  Final Constant-->" + Index.getFormula(ind));
			formPart.add(ind);
			working("}\n");
		}

		return formPart;
	}

	private DiffSeq diffSeq(final List<Integer> sequence) throws UnresolvableSequence {
		List<Integer> diffs = diffArray(sequence);

		// Validate
		boolean valid = true;

//		List<Integer> diffDiffs = diffArray(diffs);
//
//		DIFF_TYPE lastDT = DIFF_TYPE.evaluate(diffDiffs.get(0), diffDiffs.get(1));
//		for (int i = 1; i < diffDiffs.size() - 1 && valid; i++) {
//			DIFF_TYPE nextDT = DIFF_TYPE.evaluate(diffDiffs.get(i), diffDiffs.get(i + 1));
//
//			if (nextDT != lastDT) {
//				valid = false;
//			}
//		}
	
		DIFF_TYPE lastDT = DIFF_TYPE.evaluate(diffs.get(0), diffs.get(1));
		for (int i = 1; i < diffs.size() - 1 && valid; i++) {
			DIFF_TYPE nextDT = DIFF_TYPE.evaluate(diffs.get(i), diffs.get(i + 1));

			if (nextDT != lastDT) {
				valid = false;
			}
		}

		if (!valid) {
			throw new UnresolvableSequence("Evaluated sequence is not consistent");
		}

		return new DiffSeq(lastDT, diffs);
	}

	private static List<Integer> diffArray(final List<Integer> sequence) {
		List<Integer> diffs = new ArrayList<Integer>(sequence.size() - 1);
		List<Integer> workS = new ArrayList<Integer>(sequence.size());
		workS.addAll(sequence);

		do {
			diffs.add(workS.get(1) - workS.get(0));
			workS.remove(0);
		} while (workS.size() > 1);

		return diffs;
	}

	private static int factorial(final int n) {
		if (n==1) return 1;
		
		return n * factorial(n-1);
	}
	
	private static void working(final String out) {
		if (WORKING) {
			System.out.println(out);
		}
	}
	
	private static class DiffSeq {
		private DIFF_TYPE dt;
		private List<Integer> diffs;

		public DiffSeq(final DIFF_TYPE dt, final List<Integer> diffs) {
			this.dt = dt;
			this.diffs = diffs;
		}

		public Integer getDiff() {
			if (this.dt == DIFF_TYPE.COMPLETE || this.dt == DIFF_TYPE.ZEROS) {
				return diffs.get(0);
			} else {
				return null;
			}
		}
	}

	private static class Index {
		private boolean negative;
		private int multiplier;
		private int power;

		public Index(final int multiplier, final int power) {
			if (multiplier < 0) {
				this.negative = true;
				this.multiplier = -multiplier;
			}
			else {
				this.multiplier = multiplier;
			}
			
			this.power = power;
		}
		
		public static String getFormula(final List<Index> indexes) {
			String output = "";
			if (indexes.size() != 0) {
				
				for (int i = 0; i < indexes.size(); i++) {
					String sgn = indexes.get(i).negative?"- ":"+ ";
					
					if (indexes.get(i).multiplier > 0) {
						if (i == 0) {
							sgn = indexes.get(0).negative?"-":"";
						}
					}
					else {
						sgn = "";
					}
					
					output+= sgn;
					output+= indexes.get(i).toString();
				}				
			}
			
			return output;
		}
		
		public static String getFormula(final Index index) {
			final List<Index> indexOne = new ArrayList<>(1);
			indexOne.add(index);
			return getFormula(indexOne);
		}

		public String toString() {
			if (multiplier == 0)
				return "";
			else if (power == 0)
				return multiplier + "";
			else
				return (multiplier==1?"":multiplier) + "n" + (power > 1 ? "^" + power : "") + " ";
		}
	}

	private static enum DIFF_TYPE {
		ZEROS, COMPLETE, SHRINKING, GROWING;

		static DIFF_TYPE evaluate(final Integer one, final Integer two) {

			if (one < two) {
				return GROWING;
			} else if (one > two) {
				return SHRINKING;
			} else if (one == 0 && two == 0) {
				return ZEROS;
			} else {
				return DIFF_TYPE.COMPLETE;
			}
		}
	}

	private static class UnresolvableSequence extends Exception {
		public UnresolvableSequence(String message) {
			super(message);
		}
	}
}