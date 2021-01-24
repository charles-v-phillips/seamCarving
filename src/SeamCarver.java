import edu.princeton.cs.algs4.Picture;


import java.util.Arrays;

public class SeamCarver {
    private Picture picture;
    private int width;
    private int height;
    private double[][] energy;
    //private double[][] distTo;
    //private Pair[][] edgeTo;

    private boolean orient;

    public SeamCarver(Picture picture) {
        if(picture == null) throw new IllegalArgumentException();
        this.picture = picture;
        this.width = picture.width();
        this.height = picture.height();
        energy = calculateEnergy();

    }
    private double[][] calculateEnergy(){
        double[][] e = new double[height][width];
        for (int w = 0; w < width; w++) {
            for (int h = 0; h < height; h++) {
                e[h][w] = energy(w,h);
            }
        }
        return e;
    }

    public Picture picture() {return picture;}
    public int width() {return width;}
    public int height() {return height;}
    public double energy(int x, int y) {
        if(!isValidBounds(y,x)) throw new IllegalArgumentException();
        if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
            return 1000;
        } else {

            int rgbX1 = picture.getRGB(x+1,y);
            int rX1 = (rgbX1 >> 16) & 0xFF;
            int gX1 = (rgbX1 >> 8)  & 0xFF;
            int bX1 = (rgbX1 >> 0)  & 0xFF;

            int rgbX2 = picture.getRGB(x-1,y);
            int rX2 = (rgbX2 >> 16) & 0xFF;
            int gX2 = (rgbX2 >> 8)  & 0xFF;
            int bX2 = (rgbX2 >> 0)  & 0xFF;

            int rgbY1 = picture.getRGB(x,y+1);
            int rY1 = (rgbY1 >> 16) & 0xFF;
            int gY1 = (rgbY1 >> 8)  & 0xFF;
            int bY1 = (rgbY1 >> 0)  & 0xFF;

            int rgbY2 = picture.getRGB(x,y-1);
            int rY2 = (rgbY2 >> 16) & 0xFF;
            int gY2 = (rgbY2 >> 8)  & 0xFF;
            int bY2 = (rgbY2 >> 0)  & 0xFF;

            double dXRed    = rX1 - rX2;
            double dXGreen  = gX1 - gX2;
            double dXBlue   = bX1 - bX2;
            double dYRed    = rY1 - rY2;
            double dYGreen  = gY1 - gY2;
            double dYBlue   = bY1 - bY2;

            double xSq = Math.pow(dXRed,2) + Math.pow(dXGreen,2) + Math.pow(dXBlue,2);
            double ySq = Math.pow(dYRed,2) + Math.pow(dYGreen,2) + Math.pow(dYBlue,2);
            return Math.sqrt(xSq + ySq);


        }
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {

        double [][] distTo = new double[height][width];
        Pair[][] edgeTo = new Pair[height][width];
        for (double[] r : distTo) Arrays.fill(r, Double.POSITIVE_INFINITY);
        for (Pair[] r : edgeTo) Arrays.fill(r, new Pair(Integer.MAX_VALUE, Integer.MAX_VALUE));

        for(int h = 0;h < height; h++){
            distTo[h][0] = energy[h][0];
            edgeTo[h][0] = new Pair(-1,-1);
        }

        for(int w = 0; w < width-1; w++){
            for(int h = 0; h < height; h++){
                relaxHorizontal(h,w,distTo,edgeTo);
            }
        }
        int index = 0;
        double val = Double.POSITIVE_INFINITY;
        for(int h = 0; h < height; h++){
            if(distTo[h][width-1] < val){
                index = h;
                val = distTo[h][width-1];
            }

        }

        int[] rv = new int[width];
        rv[width-1] = index;
        int y = width - 1;
        while(y >= 0){
            rv[y] = index;
            index = edgeTo[index][y].h; //this line is sus
            y--;
        }


        return rv;

    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        double [][] distTo = new double[height][width];
        Pair[][] edgeTo = new Pair[height][width];
        for (double[] r : distTo) Arrays.fill(r, Double.POSITIVE_INFINITY);
        for (Pair[] r : edgeTo) Arrays.fill(r, new Pair(Integer.MAX_VALUE, Integer.MAX_VALUE));


        //relax first line of pixels, edgeTo is imaginary source (-1,-1)
        for (int w = 0; w < width; w++) {
            distTo[0][w] = energy[0][w];
            edgeTo[0][w] = new Pair(-1, -1);

        }
        //relaxing propogating down the image
        for (int h = 0; h < height-1; h++) {
            for (int w = 0; w < width; w++) {
                relaxVertical(h, w,distTo,edgeTo);
            }
        }

        int index = 0;
        double val = Double.POSITIVE_INFINITY;
        for (int w = 0; w < width ; w++) {
            if(distTo[height-1][w] < val){
                index = w;
                val = distTo[height-1][w];
            }
        }

        int[] rv = new int[height];
        rv[height - 1] = index;
        int x = height-1;
        while(x >=0){
            rv[x] = index;
            index = edgeTo[x][index].w;
            x--;
        }
        return rv;
    }

    private void relaxHorizontal(int h,int w,double [][] distTo,Pair[][] edgeTo){
        for(int i = -1; i < 2; i ++){
            try{
                if(distTo[h+i][w+1] > distTo[h][w] + energy[h + i][w+1]){
                    distTo[h+i][w+1] = distTo[h][w] + energy[h+ i][w+1];
                    edgeTo[h+i][w+1] = new Pair(h,w);
                }
            }catch(IndexOutOfBoundsException e){}
        }
    }

    private void relaxVertical(int h, int w,double[][]distTo, Pair[][]edgeTo) {
        for (int i = -1; i < 2; i++) {
            try {
                if (distTo[h + 1][w + i] > distTo[h][w] + energy[h + 1][w  + i]) { //changed from h and w to h+1, w + i
                    distTo[h + 1][w + i] = distTo[h][w] + energy[h + 1][w + i]; //changed from h and w to h+1, w + i
                    edgeTo[h + 1][w + i] = new Pair(h, w);
                }
            } catch (IndexOutOfBoundsException e) {}
        }
    }


    // remove horizontal seam from current picture
    public void removeHorizontalSeam ( int[] seam){
        if(seam == null || !isValidHorizontalSeam(seam)){throw new IllegalArgumentException();}
        Picture newPic = new Picture(width,height-1);
        for(int w = 0; w < newPic.width(); w++){
            for (int h = 0; h < newPic.height(); h++) {
                if(h >= seam[w]){
                    newPic.set(w,h,picture.get(w,h+1));
                }
                else{newPic.set(w,h,picture.get(w,h));}

            }
        }
        picture = newPic;
        this.height--;
        energy = calculateEnergy();

    }

    // remove vertical seam from current picture
    public void removeVerticalSeam ( int[] seam){
        if(seam == null || !isValidVerticalSeam(seam)){throw new IllegalArgumentException("this is not a valid seam");}
        Picture newPic = new Picture(width-1,height);
        for(int h = 0; h < newPic.height();h++){
            for(int w = 0; w < newPic.width();w++) {
                if (w >= seam[h]) {
                    newPic.set(w, h, picture.get(w + 1, h));
                }
                else{newPic.set(w,h,picture.get(w,h));}
            }



        }
        picture = newPic;
        this.width--;
        energy = calculateEnergy();


    }
    private boolean isValidBounds(int h, int w){
        return !(h<0 || h>= height || w  < 0 || w >= width);

    }

    private boolean isValidVerticalSeam(int[] seam){
        if(seam.length != height)return false;
        for(int h = 0; h < seam.length-1; h++){
            if(seam[h] >= width || seam[h] <0)return false;
            if(Math.abs(seam[h] - seam[h+1]) > 1) return false;
        }
        if(seam[seam.length-1] >= width || seam[seam.length-1] < 0) return false;
        return true;

    }
    private boolean isValidHorizontalSeam(int[] seam){
        if(seam.length != width)return false;
        for (int w = 0; w < seam.length-1; w++) {
            if (seam[w] >= height || seam[w] <0) return false;
            if (Math.abs(seam[w] - seam[w + 1]) > 1) return false;

        }
        if(seam[seam.length-1] >= height ||seam[seam.length-1] < 0)return false;
        return true;


    }


    //  unit testing (optional)
    public static void main (String[] args){
        Picture p = new Picture("6x5.png");

        SeamCarver s = new SeamCarver(p);
        s.removeVerticalSeam(s.findVerticalSeam());
        //System.out.println("HI");
        //System.out.println(p.getRGB(0,0));
        System.out.println(s.energy(4,-1));


    }

}


