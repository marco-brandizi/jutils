package uk.ac.ebi.utils.statistics;

//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

/**
 * Class for Fisher Exact test.
 * 
 * <p>This was copied and adapted from 
 * <a href = "https://github.com/HuntsmanCancerInstitute/USeq/blob/master/Source/util/gen/FisherExact.java">here</a>,
 * (commit 71b68de, 2023/07/17), since the original class is part of a bigger project concerning a different 
 * specific application domain.</p>
 * 
 * <p>TODO: It would be better to have a cached object that depends on maxSize as cache key, ie, 
 * when a requested maxSize is &lt; cached maxSize, the existing instance is returned, 
 * else a new instance is created that replaces the old one. This also requires that such
 * instance has an expiration time, a cache that evicts automatically and an ability to initialise
 * a {@link FisherExact} with a bigger size from one with a smaller one. At that point, we
 * would prefer static wrapper methods for methods like {@link #getP(int, int, int, int)}.</p>
 *
 * <p><i>Original comments</i></p>
 *
 * This does a Fisher Exact test. The Fisher's Exact test procedure calculates an exact probability value for the
 * relationship between two dichotomous variables, as found in a two by two crosstable. The program calculates the
 * difference between the data observed and the data expected, considering the given marginal and the assumptions of
 * the model of independence. It works in exactly the same way as the Chi-square test for independence; however, the
 * Chi-square gives only an estimate of the true probability value, an estimate which might not be very accurate if
 * the marginal is very uneven or if there is a small value (less than five) in one of the cells.
 *
 * It uses an array of factorials initialized at the beginning to provide speed. There could be better ways to do
 * this.
 *
 * @author Ed Buckler
 * @version $Id: FisherExact.java,v 1
 */
public class FisherExact
{
	private double[] f;
	int maxSize;

	/**
	 * constructor for FisherExact table
	 *
	 * @param maxSize is the maximum sum that will be encountered by the table (a+b+c+d)
	 */
	public FisherExact ( int maxSize )
	{
		this.maxSize = maxSize;
		f = new double[ maxSize + 1 ];
		f[ 0 ] = 0.0;
		for ( int i = 1; i <= this.maxSize; i++ )
		{
			f[ i ] = f[ i - 1 ] + Math.log ( i );
		}
	}

	/**
	 * calculates the P-value for this specific state
	 *
	 * @param a
	 *          a, b, c, d are the four cells in a 2x2 matrix
	 * @param b
	 * @param c
	 * @param d
	 * @return the P-value
	 */
	public final double getP ( int a, int b, int c, int d )
	{
		int n = a + b + c + d;
		if ( n > maxSize )
		{
			return Double.NaN;
		}
		double p;
		p = ( f[ a + b ] + f[ c + d ] + f[ a + c ] + f[ b + d ] ) - ( f[ a ] + f[ b ] + f[ c ] + f[ d ] + f[ n ] );
		return Math.exp ( p );
	}

	/**
	 * Calculates the one-tail P-value for the Fisher Exact test. Determines whether to calculate the right- or left-
	 * tail, thereby always returning the smallest p-value.
	 *
	 * a, b, c, d are the four cells in a 2x2 matrix
	 * 
	 * @return one-tailed P-value (right or left, whichever is smallest)
	 */
	public final double getCumlativeP ( int a, int b, int c, int d )
	{
		int min, i;
		int n = a + b + c + d;
		if ( n > maxSize )
		{
			return Double.NaN;
		}
		double p = 0;
		p += getP ( a, b, c, d );
		if ( ( a * d ) >= ( b * c ) )
		{
			min = ( c < b ) ? c : b;
			for ( i = 0; i < min; i++ )
			{
				p += getP ( ++a, --b, --c, ++d );
			}
		}
		if ( ( a * d ) < ( b * c ) )
		{
			min = ( a < d ) ? a : d;
			for ( i = 0; i < min; i++ )
			{
				double pTemp = getP ( --a, ++b, ++c, --d );
				p += pTemp;
			}
		}
		return p;
	}

	/**
	 * Calculates the right-tail P-value for the Fisher Exact test.
	 *
	 * a, b, c, d are the four cells in a 2x2 matrix
	 * 
	 * @return one-tailed P-value (right-tail)
	 */
	public final double getRightTailedP ( int a, int b, int c, int d )
	{
		int min, i;
		int n = a + b + c + d;
		if ( n > maxSize )
		{
			return Double.NaN;
		}
		double p = 0;

		p += getP ( a, b, c, d );
		min = ( c < b ) ? c : b;
		for ( i = 0; i < min; i++ )
		{
			p += getP ( ++a, --b, --c, ++d );

		}
		return p;
	}

	/**
	 * Calculates the left-tail P-value for the Fisher Exact test.
	 *
	 * a, b, c, d are the four cells in a 2x2 matrix
	 * 
	 * @return one-tailed P-value (left-tail)
	 */
	public final double getLeftTailedP ( int a, int b, int c, int d )
	{
		int min, i;
		int n = a + b + c + d;
		if ( n > maxSize )
		{
			return Double.NaN;
		}
		double p = 0;

		p += getP ( a, b, c, d );
		min = ( a < d ) ? a : d;
		for ( i = 0; i < min; i++ )
		{
			double pTemp = getP ( --a, ++b, ++c, --d );
			p += pTemp;
		}

		return p;
	}

	/**
	 * Calculates the two-tailed P-value for the Fisher Exact test.
	 *
	 * In order for a table under consideration to have its p-value included in the final result, it must have a p-value
	 * less than the original table's P-value, i.e. Fisher's exact test computes the probability, given the observed
	 * marginal frequencies, of obtaining exactly the frequencies observed and any configuration more extreme. By "more
	 * extreme," we mean any configuration (given observed marginals) with a smaller probability of occurrence in the
	 * same direction (one-tailed) or in both directions (two-tailed).
	 *
	 * a, b, c, d are the four cells in a 2x2 matrix
	 * 
	 * @return two-tailed P-value or NaN if the table sum exceeds the maxSize
	 */
	public final double getTwoTailedP ( int a, int b, int c, int d )
	{
		int min, i;
		int n = a + b + c + d;
		if ( n > maxSize )
		{
			return Double.NaN;
		}
		double p = 0;

		double baseP = getP ( a, b, c, d );

		int initialA = a, initialB = b, initialC = c, initialD = d;
		p += baseP;
		min = ( c < b ) ? c : b;
		for ( i = 0; i < min; i++ )
		{
			double tempP = getP ( ++a, --b, --c, ++d );
			if ( tempP <= baseP )
			{
				p += tempP;
			}
		}

		// reset the values to their original so we can repeat this process for the other side
		a = initialA;
		b = initialB;
		c = initialC;
		d = initialD;

		min = ( a < d ) ? a : d;
		for ( i = 0; i < min; i++ )
		{
			double pTemp = getP ( --a, ++b, ++c, --d );
			if ( pTemp <= baseP )
			{
				p += pTemp;
			}
		}
		return p;
	}

	/** Returns OR, lower, upper */
	public static double[] getOddsRatioAnd95thConfidenceInterval ( double a, double b, double c, double d )
	{
		// Where zeros cause problems with computation of the odds ratio or its standard error, 0.5 is added to all cells
		// (a, b, c, d) (Pagano & Gauvreau, 2000; Deeks & Higgins, 2010).
		if ( a == 0.0 || b == 0.0 || c == 0.0 || d == 0.0 )
		{
			a += 0.5;
			b += 0.5;
			c += 0.5;
			d += 0.5;
		}

		double oddsRatio = ( a * d ) / ( b * c );

		double inner = 1.96 * Math.sqrt ( 1.0 / a + 1.0 / b + 1.0 / c + 1.0 / d );
		double lnOR = Math.log ( oddsRatio );

		// Upper 95% CI = e ^ [ln(OR) + 1.96 sqrt(1/a + 1/b + 1/c + 1/d)]
		double upper = Math.pow ( Math.E, ( lnOR + inner ) );

		// Lower 95% CI = e ^ [ln(OR) - 1.96 sqrt(1/a + 1/b + 1/c + 1/d)]
		double lower = Math.pow ( Math.E, ( lnOR - inner ) );

		return new double[] { oddsRatio, lower, upper };
	}

	/**
	 * Measure of how far from independence the 2x2 table is. 1= independent. Ratio of ratios. Will return Infinity or 0
	 * if cells are zero.
	 */
	public static double getOddsRatio ( double a, double b, double c, double d )
	{
		return ( ( a * d ) / ( b * c ) );
	}
}