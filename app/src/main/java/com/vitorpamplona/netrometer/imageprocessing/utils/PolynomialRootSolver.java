/**
 * Copyright (c) 2024 Vitor Pamplona
 *
 * This program is offered under a commercial and under the AGPL license.
 * For commercial licensing, contact me at vitor@vitorpamplona.com.
 * For AGPL licensing, see below.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * This application has not been clinically tested, approved by or registered in any health agency.
 * Even though this repository grants licenses to use to any person that follow it's license,
 * any clinical or commercial use must additionally follow the laws and regulations of the
 * pertinent jurisdictions. Having a license to use the source code does not imply on having
 * regulatory approvals to use or market any part of this code.
 */
package com.vitorpamplona.netrometer.imageprocessing.utils;

import org.ejml.data.Complex64F;
import org.ejml.data.DenseMatrix64F;
import org.ejml.factory.DecompositionFactory;
import org.ejml.factory.EigenDecomposition;

/**
 * Root solver for polynomials using eigenvalue decomposition.
 * Useful for inverse solving an equation (i.e. find x given y) of the form:  
 * </p> y = A*x^3 + B*x^2 + C*x + D
 * </p> Constructor takes the coefficients in this order: [(D-y) C B A].  Can be used
 * for smaller or larger order polynomials, just add the appropriate 
 * coefficients.
 * </p> <b>Example:</b> 
 * </p> Given a polynomial y = A*x^2 + B*x + C, first solve for y(x) = 0.
 * </p> 0 = A*x^2 + B*x + (C-y)
 * </p> The coefficients are now [(C-y) B A]
 * </p> double x = <b>new</b> PolynomialRootSolver.solve(C-y, B, A).getFirstRealRoot();
 * </p>
 */
public class PolynomialRootSolver {

	private Complex64F[] roots;
	
	public PolynomialRootSolver() {}
	
    public PolynomialRootSolver solve(double... coefficients) {
    	
    	// N-th order polynomial
        int N = coefficients.length-1;

        // Construct the companion matrix
        DenseMatrix64F c = new DenseMatrix64F(N,N);

        double a = coefficients[N];
        for( int i = 0; i < N; i++ ) {
            c.set(i,N-1,-coefficients[i]/a);
        }
        for( int i = 1; i < N; i++ ) {
            c.set(i,i-1,1);
        }

        // Use generalized eigenvalue decomposition to find the roots
        EigenDecomposition<DenseMatrix64F> evd =  DecompositionFactory.eig(N, false);

        evd.decompose(c);

        Complex64F[] r = new Complex64F[N];

        for( int i = 0; i < N; i++ ) {
            r[i] = evd.getEigenvalue(i);
        }

        roots = r;
        
        return this;

    }
    
    
    public double getFirstRealRoot() {
    	
        // Get the real root (Pick first one. There should only by one.. highlander)
        double x = Double.NaN;
        for (int i=0; i<roots.length; i++) {
        	
        	double mag = roots[i].getMagnitude();
        	
        	if (mag > -20 && mag < 20) {
                x = roots[i].getReal();
                break;
        	}
        }
        
        return x;
    }
    
    
    public Complex64F[] getRoots() {
    	return roots;
    }
    
    
	public void testRootSolver() {
		
		double[] coefficients = {1, 2.01 / 100, 4.1199 / 10000, 6.0319 / 1000000};

		// Hard set coefficients
							
		double baseRadius = 143.0087;
		double usedRadius = baseRadius * 0.9997;
		
		// Scale formula with base radius
		coefficients[0] *= baseRadius;
		coefficients[1] *= baseRadius;
		coefficients[2] *= baseRadius;
		coefficients[3] *= baseRadius;
		
		// Rearrange to y(x) = 0
		coefficients[0] -= usedRadius;
		
		// Find diopter given
		double diopter = new PolynomialRootSolver().solve(coefficients).getFirstRealRoot();
				
	}
	
}
