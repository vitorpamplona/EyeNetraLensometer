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

import com.vitorpamplona.netrometer.NetrometerApplication;

import org.ejml.simple.SimpleMatrix;

import java.util.List;



/**
 * Sinusoidal regression fit to a set of data points using a 
 * Gauss-Newton regression solver.  Given a fixed frequency (B), 
 * finds the amplitude (A), phase (C), and offset (D) of the 
 * sinusoidal expression: y = A * sin(B*x + C) + D
 */
public class GaussNewtonSineFitting {

	public static final double EPSILON = 0.01;

	private int iterations;
	private double DC;
	private double frequency;
	private SimpleMatrix G = new SimpleMatrix(3,1);
	
	
	public GaussNewtonSineFitting(int frequency, int iterations, double dampingCoefficient) {
		
		this.frequency = frequency;
		this.iterations = iterations;
		this.DC = dampingCoefficient;

	}
	
	public SinusoidalModel calculate(List<Polar> data) {
		
		// check if ready
		if (data == null || data.size() == 0) {
			return new SinusoidalModel();
		}
		
		MinMaxMean m = new MinMaxMean(data);
		
		// define starting point
		G.set(0, 0, ( m.max - m.min ) / 2 );
		G.set(1, 0, 0 );
		G.set(2, 0, m.mean);
		
		SimpleMatrix J = new SimpleMatrix((int) data.size(), 3);
		SimpleMatrix r = new SimpleMatrix((int) data.size(), 1);
		SimpleMatrix Jt, delta;

		for (int i=0; i<iterations; i++) {
			
			// build Jacobian and residual matrix
			for (int j=0; j<data.size(); j++) {
							
		        J.set(j, 0, Math.sin(frequency*data.get(j).theta + G.get(1)));
		        J.set(j, 1, G.get(0) * Math.cos(frequency*data.get(j).theta + G.get(1)));
		        J.set(j, 2, 1.0);
		        
		        r.set(j, 0, G.get(0) * Math.sin(frequency*data.get(j).theta + G.get(1)) + G.get(2) - data.get(j).r);

			}
			
			// Gauss-Newton
			Jt = J.transpose();
			delta = Jt.mult(J).invert().mult(Jt).mult(r);
			G = G.minus(delta.scale(DC));
		}

		double A = G.get(0);   // amplitude
		double B = frequency;  // frequency
		double C = G.get(1);   // phase
		double D = G.get(2);   // offset

		// make amplitude positive only and fix phase (sine is symmetric)
		if(A<0) {
			A *= -1;
			C += Math.PI;
		}
		
		return new SinusoidalModel(A, B, C, D);
		
	}

	public SinusoidalModel calculate(Polar[] data) {

		// check if ready
		if (data == null || data.length == 0) {
			return new SinusoidalModel();
		}

		MinMaxMean m = new MinMaxMean(data);
        if (m.validPoints/(float)data.length<=0.10) {
//				Log.e("Gauss","m.validPoints/(float)data.length= "+(m.validPoints/(float)data.length));
				return null;
		} // Eliminate calculation if 25%+ data is NaN


		// define starting point
		G.set(0, 0, ( m.max - m.min ) / 2 );
		G.set(1, 0, 0 );
		G.set(2, 0, m.mean);

		SimpleMatrix J = new SimpleMatrix(m.validPoints, 3);
		SimpleMatrix r = new SimpleMatrix(m.validPoints, 1);

		SimpleMatrix Jt, delta;

		double A=0, B=0, C =0, D = 0;
		double prevA=999, prevB=999, prevC =999, prevD = 999;
		double err,sqsum=0;
		int validDataPoints=0;

		for (int i=0; i<iterations; i++) {

//			sqsum=0;
			// build Jacobian and residual matrix
            int JIndx = 0;
			for (int j=0; j<data.length; j++) {
				if (!Double.isNaN(data[j].r)) {
					J.set(JIndx, 0, Math.sin(frequency*data[j].theta + G.get(1)));
					J.set(JIndx, 1, G.get(0) * Math.cos(frequency*data[j].theta + G.get(1)));
					J.set(JIndx, 2, 1.0);

					r.set(JIndx, 0, G.get(0) * Math.sin(frequency*data[j].theta + G.get(1)) + G.get(2) - data[j].r);
                    JIndx++;
				}
			}

			// Gauss-Newton
			Jt = J.transpose();
			delta = Jt.mult(J).invert().mult(Jt).mult(r);
			G = G.minus(delta.scale(DC));

			// get values
			prevA = A;   // amplitude
			prevB = B;  // frequency
			prevC = C;   // phase
			prevD = D;   // offset

			A = G.get(0);   // amplitude
			B = frequency;  // frequency
			C = G.get(1);   // phase
			D = G.get(2);   // offset



			if (vectorLength(A, B, C, D, prevA, prevB, prevC, prevD) < EPSILON) {
				break;
			}
		}

		for(int ii=0;ii<data.length;ii++){
			err =( data[ii].r - (A*Math.sin(frequency*data[ii].theta+C)+D) );
//				Log.e("Gauss", "ii "+ii+", err "+err+", A*Math.sin(frequency*data[ii].theta+C)+D: "+(A*Math.sin(frequency*data[ii].theta+C)+D)+", data[ii].r "+data[ii].r );
			if ( !Double.isNaN(err)){
				validDataPoints++;
				sqsum = sqsum+ err*err;
			}
		}

		double std =  Math.sqrt(sqsum / validDataPoints);
//		DecimalFormat df=new DecimalFormat("#.##");
//		Log.e("Gauss", ",,,,,,,,,, A " + df.format(A) + ", B " + df.format(B) + ", C " + df.format(C) + ",D " + df.format(D) + "               sqRoot " + df.format(Math.sqrt(sqsum / validDataPoints)) + " sqsum " + df.format(sqsum)+ " validDataPoints " + validDataPoints);


		// make amplitude positive only and fix phase (sine is symmetric)
		if(A<0) {
			A *= -1;
			C += Math.PI;
		}

		// Leveraging noise on cyl
		if(std>0.35) return null;

		return new SinusoidalModel(
				NetrometerApplication.get().getDevice().getFineTunedDiopterCoeffA(A, B, C, D, std),
				NetrometerApplication.get().getDevice().getFineTunedDiopterCoeffB(A, B, C, D, std),
				NetrometerApplication.get().getDevice().getFineTunedDiopterCoeffC(A, B, C, D, std),
				NetrometerApplication.get().getDevice().getFineTunedDiopterCoeffD(A, B, C, D, std)
		);

	}

	public double vectorLength(double v1x, double v1y, double v1z, double v1w, double v2x, double v2y, double v2z, double v2w) {
		double diffX = v1x - v2x;
		double diffY = v1y - v2y;
		double diffZ = v1z - v2z;
		double diffW = v1w - v2w;

		return Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ + diffW * diffW);
	}

	private class MinMaxMean {
		
		public double min;
		public double max;
		public double mean;
		private int totalPoints;
		public int validPoints;
		
		public MinMaxMean(List<Polar> collection) {
			
			totalPoints = collection.size();
			validPoints = 0;
			min = Double.MAX_VALUE;
			max = Double.MIN_VALUE;
			mean = 0;
			
			for (Polar c : collection) {
				if (Double.isNaN(c.r)) continue;
				if (c.r > max) max = c.r;
				if (c.r < min) min = c.r;
				mean += c.r;
				validPoints++;
			}
			
			mean /= validPoints;
			
		}

		public MinMaxMean(Polar[] collection) {

			totalPoints = collection.length;
			validPoints = 0;
			min = Double.MAX_VALUE;
			max = Double.MIN_VALUE;
			mean = 0;

			for (Polar c : collection) {
				if (Double.isNaN(c.r)) continue;
				if (c.r > max) max = c.r;
				if (c.r < min) min = c.r;
				mean += c.r;
				validPoints++;
			}

			mean /= validPoints;

		}

		public int getNumTotalPoints() {
			return totalPoints;
		}
		
		public int getNumValidPoints() {
			return validPoints;
		}

	}
	
	
	public static class SinusoidalModel {
		
		public double amplitude;
		public double frequency;
		public double phase;
		public double offset;
		
		public SinusoidalModel() {
			
			this.amplitude = 0;
			this.frequency = 0;
			this.phase = 0;
			this.offset = 0;
			
		}
		
		public SinusoidalModel(double amplitude, double frequency, double phase, double offset) {
			
			this.amplitude = amplitude;
			this.frequency = frequency;
			this.phase = phase;
			this.offset = offset;
			
		}
		
	}
	
}
