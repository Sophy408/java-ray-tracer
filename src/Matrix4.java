class Matrix4 {
    double[][] m = new double[4][4];

    Matrix4() {
        for (int i = 0; i < 4; i++) {
            m[i][i] = 1.0;
        }
    }

    Vektor3 transformierePunkt(Vektor3 v) {
        return transformiere(v);
    }

    Vektor3 transformiereRichtung(Vektor3 v) {
        double nx =
                m[0][0] * v.x +
                        m[0][1] * v.y +
                        m[0][2] * v.z;

        double ny =
                m[1][0] * v.x +
                        m[1][1] * v.y +
                        m[1][2] * v.z;

        double nz =
                m[2][0] * v.x +
                        m[2][1] * v.y +
                        m[2][2] * v.z;

        return new Vektor3(nx, ny, nz);
    }

    static Matrix4 rotationY(double winkelGrad) {
        Matrix4 matrix = new Matrix4();

        double w = Math.toRadians(winkelGrad);
        double cos = Math.cos(w);
        double sin = Math.sin(w);

        matrix.m[0][0] = cos;
        matrix.m[0][2] = sin;
        matrix.m[2][0] = -sin;
        matrix.m[2][2] = cos;

        return matrix;
    }

    Matrix4 mal(Matrix4 andere) {
        Matrix4 ergebnis = new Matrix4();

        for (int zeile = 0; zeile < 4; zeile++) {
            for (int spalte = 0; spalte < 4; spalte++) {
                ergebnis.m[zeile][spalte] = 0;

                for (int i = 0; i < 4; i++) {
                    ergebnis.m[zeile][spalte] += this.m[zeile][i] * andere.m[i][spalte];
                }
            }
        }

        return ergebnis;
    }

    static Matrix4 translation(double x, double y, double z) {
        Matrix4 matrix = new Matrix4();

        matrix.m[0][3] = x;
        matrix.m[1][3] = y;
        matrix.m[2][3] = z;

        return matrix;
    }

    static Matrix4 skalierung(double x, double y, double z) {
        Matrix4 matrix = new Matrix4();

        matrix.m[0][0] = x;
        matrix.m[1][1] = y;
        matrix.m[2][2] = z;

        return matrix;
    }

    Vektor3 transformiere(Vektor3 v) {
        double nx =
                m[0][0] * v.x +
                        m[0][1] * v.y +
                        m[0][2] * v.z +
                        m[0][3];

        double ny =
                m[1][0] * v.x +
                        m[1][1] * v.y +
                        m[1][2] * v.z +
                        m[1][3];

        double nz =
                m[2][0] * v.x +
                        m[2][1] * v.y +
                        m[2][2] * v.z +
                        m[2][3];

        return new Vektor3(nx, ny, nz);
    }
}