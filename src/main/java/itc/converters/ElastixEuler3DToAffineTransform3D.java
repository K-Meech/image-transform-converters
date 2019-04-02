package itc.converters;

import itc.transforms.ElastixEulerTransform3D;
import net.imglib2.realtransform.AffineTransform3D;

public class ElastixEuler3DToAffineTransform3D
{

	/**
	 * From elastix-4.9.0 manual:
	 *
	 * Rigid: (EulerTransform) A rigid transformation is defined as:
	 * Tμ(x) = R(x − c) + t + c,
	 * with the matrix R a rotation matrix (i.e. orthonormal and proper),
	 * c the centre of rotation, and t translation again.
	 * The image is treated as a rigid body, which can translate and rotate,
	 * but cannot be scaled/stretched.
	 * The rotation matrix is parameterised by the Euler angles (one in 2D, three in 3D).
	 * The parameter vector μ consists of the Euler angles (in rad)
	 * and the translation vector.
	 * In 2D, this gives a vector of length 3: μ = (θz , tx , ty )T ,
	 * where θz denotes the rotation around the axis normal to the image.
	 * In 3D, this gives a vector of length 6: μ = (θx,θy,θz,tx,ty,tz)T .
	 * The centre of rotation is not part of μ; it is a fixed setting,
	 * usually the centre of the image.
	 *
	 * Note: Elastix units are always millimeters
	 *
	 * @param elastixEulerTransform3D
	 *
	 * @param targetImageVoxelSpacingInMicrometer
	 *
	 * @return an affine transform performing the Euler transform
	 */
	public AffineTransform3D getAffineTransform3D(
			ElastixEulerTransform3D elastixEulerTransform3D,
			double[] targetImageVoxelSpacingInMicrometer )
	{

		final double[] angles = elastixEulerTransform3D.getRotationAnglesInRadians();

		final double[] translationInMillimeters = elastixEulerTransform3D.getTranslationInMillimeters();
		final double[] translationInPixels = new double[ 3 ];
		for ( int d = 0; d < 3; ++d )
		{
			translationInPixels[ d ] = translationInMillimeters[ d ] / ( 0.001 * targetImageVoxelSpacingInMicrometer[ d ] );
		}

		final double[] rotationCenterInMillimeters = elastixEulerTransform3D.getRotationCenterInMillimeters();
		final double[] rotationCentreVectorInPixelsPositive = new double[ 3 ];
		final double[] rotationCentreVectorInPixelsNegative = new double[ 3 ];

		for ( int d = 0; d < 3; ++d )
		{
			rotationCentreVectorInPixelsPositive[ d ] = rotationCenterInMillimeters[ d ] / ( 0.001 * targetImageVoxelSpacingInMicrometer[ d ] );
			rotationCentreVectorInPixelsNegative[ d ] = - rotationCenterInMillimeters[ d ] / ( 0.001 * targetImageVoxelSpacingInMicrometer[ d ] );
		}


		final AffineTransform3D transform3D = new AffineTransform3D();

		// rotate around rotation centre
		//
		transform3D.translate( rotationCentreVectorInPixelsNegative ); // + or - ??
		for ( int d = 0; d < 3; ++d)
		{
			transform3D.rotate( d, angles[ d ]);
		}
		final AffineTransform3D translateBackFromRotationCentre = new AffineTransform3D();
		translateBackFromRotationCentre.translate( rotationCentreVectorInPixelsPositive );
		transform3D.preConcatenate( translateBackFromRotationCentre );

		// translate
		//
		transform3D.translate( translationInPixels );

		return transform3D;
	}
}