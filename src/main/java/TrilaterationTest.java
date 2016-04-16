import Trilateration.LinearLeastSquaresSolver;
import Trilateration.NonLinearLeastSquaresSolver;
import Trilateration.TrilaterationFunction;



import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;



public class TrilaterationTest {

	public static void main(String[] args) {
	
		double[][] positions = new double[][] { { 1.48 , 0 }, { 3.2, 3.2 }, { 3.0 , 0} };
		double[] distances = new double[] { 1.3, 1.5, 3 };

		NonLinearLeastSquaresSolver solver = new NonLinearLeastSquaresSolver(new TrilaterationFunction(positions, distances), new LevenbergMarquardtOptimizer());
		Optimum optimum = solver.solve();
		
		// the answer
		double[] centroid = optimum.getPoint().toArray();

		// error and geometry information; may throw SingularMatrixException depending the threshold argument provided
		RealVector standardDeviation = optimum.getSigma(0);
		RealMatrix covarianceMatrix = optimum.getCovariances(0);
		
		System.out.println(centroid[0]);
		System.out.println(standardDeviation);
		System.out.println(covarianceMatrix);
		
		
	}

	
	
}
