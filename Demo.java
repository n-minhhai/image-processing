import java.io.*;
import java.util.TreeSet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;
import java.util.Random;
import java.util.stream.*;
import java.util.Arrays;
 
public class Demo extends Component implements ActionListener {
    
    //************************************
    // List of the options(Original, Negative); correspond to the cases:
    //************************************
    private BufferedImage bi, biFiltered;   // the input image saved as bi;//
    int w, h;
    static String image_1 = "images/Barbara.bmp";
    static String image_2 = "images/alpha.bmp";

    String descs[] = {
        "Original", 
        "Negative",
        "Display 2 Images",
        "Rescale",
        "Shift",
        "Random Shift",
        "Add",
        "Subtract",
        "Multiply",
        "Divide",
        "NOT",
        "AND",
        "OR",
        "XOR",
        "MyNegative",
        "Log",
        "Power-Law",
        "Random Look-Up Table",
        "Bit-place slicing",
        "Histogram Equalization",
        "Display Histogram",
        "Averaging",
        "Weighted Averaging",
        "4-Neighbour Laplacian",
        "8-Neighbour Laplacian",
        "4-Neighbour Laplacian Enhancement",
        "8-Neighbour Laplacian Enhancement",
        "Roberts 1",
        "Roberts 2",
        "Sobel X",
        "Sobel Y",
        "Gaussian",
        "Laplacian of Gaussian",
        "Add Salt-and-Pepper",
        "Min Filter",
        "Max Filter",
        "Mid-Point Filter",
        "Median Filter",
        "Histogram Mean",
        "Histogram Std",
        "Simple Thresholding",
        "Automated Thresholding",
    };
 
    double power = 0.4; // Power-Law
    int shift_value = 50;
    double scale_value = 0.5;
    int kbit = 3; // BitPlane
    double probability = 0.3; // Salt-and-Pepper

    // window sizes
    int minFilterWindow = 3;
    int maxFilterWindow = 3;
    int midPointFilterWindow = 3;

    // threshold value for threhsolding
    int threshold = 100;
    int automated_threshold = 1;



    BufferedImage lastImage = null;
    int opIndex;  //option index for 
    int lastOp;

    double[][] mean_mask = new double[][]{
        {(double)1/9, (double)1/9, (double)1/9},
        {(double)1/9, (double)1/9, (double)1/9},
        {(double)1/9, (double)1/9, (double)1/9},
    };

    double[][] weighted_mean_mask = new double[][]{
        {(double)1/16, (double)2/16, (double)1/16},
        {(double)2/16, (double)4/16, (double)2/16},
        {(double)1/16, (double)2/16, (double)1/16},
    };

    double[][] Laplacian_4 = new double[][]{
        {0, -1, 0},
        {-1, 4, -1},
        {0, -1, 0},
    };


    double[][] Laplacian_8 = new double[][]{
        {-1, -1, -1},
        {-1, 8, -1},
        {-1, -1, -1},
    };

    double[][] LaplacianEnhancement_4 = new double[][]{
        {0, -1, 0},
        {-1, 5, -1},
        {0, -1, 0},
    };

    double[][] LaplacianEnhancement_8 = new double[][]{
        {-1, -1, -1},
        {-1, 9, -1},
        {-1, -1, -1},
    };

    double[][] Roberts_1 = new double[][]{
        {0, 0, 0},
        {0, 0, -1},
        {0, 1, 0},
    };

    double[][] Roberts_2 = new double[][]{
        {0, 0, 0},
        {0, -1, 0},
        {0, 0, 1},
    };

    double[][] Sobel_X = new double[][]{
        {-1, 0, 1},
        {-2, 0, 2},
        {-1, 0, 1},
    };

    double[][] Sobel_Y = new double[][]{
        {-1, -2, -1},
        {0, 0, 0},
        {1, 2, 1},
    };

    double[][] Gaussian = new double[][]{
        {(double)1/273, (double)4/273, (double)7/273, (double)4/273, (double)1/273},
        {(double)4/273, (double)16/273, (double)26/273, (double)16/273, (double)4/273},
        {(double)7/273, (double)26/273, (double)41/273, (double)26/273, (double)7/273},
        {(double)4/273, (double)16/273, (double)26/273, (double)16/273, (double)4/273},
        {(double)1/273, (double)4/273, (double)7/273, (double)4/273, (double)1/273}
    };

    double[][] LoG = new double[][]{
        {0, 0, -1, 0, 0},
        {0, -1, -2, -1, 0},
        {-1, -2, 16, -2, -1},
        {0, -1, -2, -1, 0},
        {0, 0, -1, 0, 0}
    };


    public Demo(String img) {
        try {
            bi = ImageIO.read(new File(img));

            w = bi.getWidth(null);
            h = bi.getHeight(null);
            System.out.println(bi.getType());
            if (bi.getType() != BufferedImage.TYPE_INT_RGB) {
                BufferedImage bi2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                Graphics big = bi2.getGraphics();
                big.drawImage(bi, 0, 0, null);
                biFiltered = bi = bi2;
            }
        } catch (IOException e) {      // deal with the situation that th image has problem;/
            System.out.println("Image could not be read");

            System.exit(1);
        }
    }                         
 
    public Dimension getPreferredSize() {
        return new Dimension(w, h);
    }
 

    String[] getDescriptions() {
        return descs;
    }

    // Return the formats sorted alphabetically and in lower case
    public String[] getFormats() {
        String[] formats = {"bmp","gif","jpeg","jpg","png"};
        TreeSet<String> formatSet = new TreeSet<String>();
        for (String s : formats) {
            formatSet.add(s.toLowerCase());
        }
        return formatSet.toArray(new String[0]);
    }
 
 

    void setOpIndex(int i) {
        opIndex = i;
    }
 
    public void paint(Graphics g) { //  Repaint will call this function so the image will change.
        filterImage();      

        g.drawImage(biFiltered, 0, 0, null);
    }
 

    //************************************
    //  Convert the Buffered Image to Array
    //************************************
    private static int[][][] convertToArray(BufferedImage image){
      int width = image.getWidth();
      int height = image.getHeight();

      int[][][] result = new int[width][height][4];

      for (int y = 0; y < height; y++) {
         for (int x = 0; x < width; x++) {
            int p = image.getRGB(x,y);
            int a = (p>>24)&0xff;
            int r = (p>>16)&0xff;
            int g = (p>>8)&0xff;
            int b = p&0xff;

            result[x][y][0]=a;
            result[x][y][1]=r;
            result[x][y][2]=g;
            result[x][y][3]=b;
         }
      }
      return result;
    }

    //************************************
    //  Convert the  Array to BufferedImage
    //************************************
    public BufferedImage convertToBimage(int[][][] TmpArray){

        int width = TmpArray.length;
        int height = TmpArray[0].length;

        BufferedImage tmpimg=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                int a = TmpArray[x][y][0];
                int r = TmpArray[x][y][1];
                int g = TmpArray[x][y][2];
                int b = TmpArray[x][y][3];
                
                //set RGB value

                int p = (a<<24) | (r<<16) | (g<<8) | b;
                tmpimg.setRGB(x, y, p);

            }
        }
        return tmpimg;
    }


    //************************************
    //  Example:  Image Negative
    //************************************
    public BufferedImage ImageNegative(BufferedImage timg){
        int width = timg.getWidth();
        int height = timg.getHeight();

        int[][][] ImageArray = convertToArray(timg);          //  Convert the image to array

        // Image Negative Operation:
        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = 255-ImageArray[x][y][1];  //r
                ImageArray[x][y][2] = 255-ImageArray[x][y][2];  //g
                ImageArray[x][y][3] = 255-ImageArray[x][y][3];  //b
            }
        }
        
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }


    //************************************
    //  Your turn now:  Add more function below



    // Display 2 images
    public BufferedImage TwoImages(String image_1, String image_2){
        BufferedImage joined_image = null;
        try {
            BufferedImage img1 = ImageIO.read(new File(image_1));
            BufferedImage img2 = ImageIO.read(new File(image_2));
    
            int wid = img1.getWidth()+img2.getWidth();
            int height = Math.max(img1.getHeight(),img2.getHeight());

            // Join two images
            joined_image = new BufferedImage(wid,height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = joined_image.createGraphics();
            Color oldColor = g2.getColor();

            g2.setPaint(Color.WHITE);
            g2.fillRect(0, 0, wid, height);

            g2.drawImage(img1, null, 0, 0);
            g2.drawImage(img2, null, img1.getWidth(), 0);
            g2.dispose();
            return joined_image;
        } catch (IOException e) {
            System.out.println("Image could not be read");
            System.exit(1);
        }
        return joined_image;
    }
    
    // Display second image
    public void displaySecondImage(String img){
        JFrame f = new JFrame("Image Processing Demo");
        Demo de = new Demo(img);
        f.add("Center", de);
        JButton b = new JButton("Undo");
        b.setActionCommand("Undo");
        b.addActionListener(de);
        JComboBox choices = new JComboBox(de.getDescriptions());
        choices.setActionCommand("SetFilter");
        choices.addActionListener(de);
        JComboBox formats = new JComboBox(de.getFormats());
        formats.setActionCommand("Formats");
        formats.addActionListener(de);
        JPanel panel = new JPanel();
        panel.add(b);
        panel.add(choices);
        panel.add(new JLabel("Save As"));
        panel.add(formats);
        f.add("North", panel);
        f.pack();
        f.setVisible(true);
    }

    // Return last image
    public BufferedImage getLastImage(){
        if (biFiltered==null){
            return bi;
        }
        return biFiltered;
    }

    // Lab2 exercise 1
    // Rescale
    public BufferedImage rescale(BufferedImage img, double factor){

        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);          //  Convert the image to array

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = (int)(ImageArray[x][y][1]*factor);  //r
                ImageArray[x][y][2] = (int)(ImageArray[x][y][2]*factor);  //g
                ImageArray[x][y][3] = (int)(ImageArray[x][y][3]*factor);  //b

                if (ImageArray[x][y][1] > 255){
                    ImageArray[x][y][1] = 255;
                }
                else if (ImageArray[x][y][1] < 0){
                    ImageArray[x][y][1] = 0;
                }
                if (ImageArray[x][y][2] > 255){
                    ImageArray[x][y][2] = 255;
                }
                else if (ImageArray[x][y][2] < 0){
                    ImageArray[x][y][2] = 0;
                }
                if (ImageArray[x][y][3] > 255){
                    ImageArray[x][y][3] = 255;
                }
                else if (ImageArray[x][y][3] < 0){
                    ImageArray[x][y][3] = 0;
                }
            }
        }
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    // Lab2 exercise 2
    // Shifting
    public BufferedImage shift(BufferedImage img, int shift){

        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);          //  Convert the image to array

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = (int)(ImageArray[x][y][1]+shift);  //r
                ImageArray[x][y][2] = (int)(ImageArray[x][y][2]+shift);  //g
                ImageArray[x][y][3] = (int)(ImageArray[x][y][3]+shift);  //b

                if (ImageArray[x][y][1] > 255){
                    ImageArray[x][y][1] = 255;
                }
                else if (ImageArray[x][y][1] < 0){
                    ImageArray[x][y][1] = 0;
                }
                if (ImageArray[x][y][2] > 255){
                    ImageArray[x][y][2] = 255;
                }
                else if (ImageArray[x][y][2] < 0){
                    ImageArray[x][y][2] = 0;
                }
                if (ImageArray[x][y][3] > 255){
                    ImageArray[x][y][3] = 255;
                }
                else if (ImageArray[x][y][3] < 0){
                    ImageArray[x][y][3] = 0;
                }
            }
        }
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    // Lab2 exercise 3
    // Value shifting and rescaling
    public BufferedImage random_shift(BufferedImage img){
        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);          //  Convert the image to array

        Random rand = new Random();
        int upperShift = 100;
        int upperScale = 2;

        int min_r = 255;
        int min_g = 255;
        int min_b = 255;
        int max_r = 0;
        int max_g = 0;
        int max_b = 0;


        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){

                int randomShift = rand.nextInt(upperShift);
                double randomScale = 2 * rand.nextDouble();

                ImageArray[x][y][1] = (int)randomScale * (ImageArray[x][y][1]+randomShift);  //r
                ImageArray[x][y][2] = (int)randomScale * (ImageArray[x][y][2]+randomShift);  //g
                ImageArray[x][y][3] = (int)randomScale * (ImageArray[x][y][3]+randomShift);  //b

                if (ImageArray[x][y][1] > max_r){
                    max_r = ImageArray[x][y][1];
                }
                if (ImageArray[x][y][1] < min_r){
                    min_r = ImageArray[x][y][1];
                }

                if (ImageArray[x][y][2] > max_g){
                    max_g = ImageArray[x][y][2];
                }
                if (ImageArray[x][y][2] < min_g){
                    min_g = ImageArray[x][y][2];
                }

                if (ImageArray[x][y][3] > max_b){
                    max_b = ImageArray[x][y][3];
                }               
                if (ImageArray[x][y][3] < min_b){
                    min_b = ImageArray[x][y][3];
                }
            }
        }
        
        for (int y=0; y<height; y++){
            for (int x=0; x<width; x++){
                ImageArray[x][y][1] = (255*(ImageArray[x][y][1]-min_r)) / (max_r-min_r);
                ImageArray[x][y][2] = (255*(ImageArray[x][y][2]-min_g)) / (max_g-min_g);
                ImageArray[x][y][3] = (255*(ImageArray[x][y][3]-min_b)) / (max_b-min_b);
            }
        }
        return convertToBimage(ImageArray);
    }

    // Lab3 exercise 1
    // Addition
    public BufferedImage add(BufferedImage img1, BufferedImage img2){
        int width = img1.getWidth();
        int height = img1.getHeight();

        int[][][] ImageArray1 = convertToArray(img1);
        int[][][] ImageArray2 = convertToArray(img2);
        int[][][] ResultImage = new int[width][height][4];

        int min_r = 255;
        int min_g = 255;
        int min_b = 255;
        int max_r = 0;
        int max_g = 0;
        int max_b = 0;

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ResultImage[x][y][1] = ImageArray1[x][y][1] + ImageArray2[x][y][1];
                ResultImage[x][y][2] = ImageArray1[x][y][2] + ImageArray2[x][y][2];
                ResultImage[x][y][3] = ImageArray1[x][y][3] + ImageArray2[x][y][3];

                if (ResultImage[x][y][1] > max_r){
                    max_r = ResultImage[x][y][1];
                }
                if (ResultImage[x][y][1] < min_r){
                    min_r = ResultImage[x][y][1];
                }

                if (ResultImage[x][y][2] > max_g){
                    max_g = ResultImage[x][y][2];
                }
                if (ResultImage[x][y][2] < min_g){
                    min_g = ResultImage[x][y][2];
                }

                if (ResultImage[x][y][3] > max_b){
                    max_b = ResultImage[x][y][3];
                }               
                if (ResultImage[x][y][3] < min_b){
                    min_b = ResultImage[x][y][3];
                }
            }
        }

        for (int y=0; y<height; y++){
            for (int x=0; x<width; x++){
                ResultImage[x][y][1] = (255*(ResultImage[x][y][1]-min_r)) / (max_r-min_r);
                ResultImage[x][y][2] = (255*(ResultImage[x][y][2]-min_g)) / (max_g-min_g);
                ResultImage[x][y][3] = (255*(ResultImage[x][y][3]-min_b)) / (max_b-min_b);
            }
        }
        return convertToBimage(ResultImage);
    }


    // Subtract
    public BufferedImage subtract(BufferedImage img1, BufferedImage img2){
        int width = img1.getWidth();
        int height = img1.getHeight();

        int[][][] ImageArray1 = convertToArray(img1);
        int[][][] ImageArray2 = convertToArray(img2);
        int[][][] ResultImage = new int[width][height][4];

        int min_r = 255;
        int min_g = 255;
        int min_b = 255;
        int max_r = 0;
        int max_g = 0;
        int max_b = 0;

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ResultImage[x][y][1] = Math.abs(ImageArray1[x][y][1] - ImageArray2[x][y][1]);
                ResultImage[x][y][2] = Math.abs(ImageArray1[x][y][2] - ImageArray2[x][y][2]);
                ResultImage[x][y][3] = Math.abs(ImageArray1[x][y][3] - ImageArray2[x][y][3]);

                if (ResultImage[x][y][1] > max_r){
                    max_r = ResultImage[x][y][1];
                }
                if (ResultImage[x][y][1] < min_r){
                    min_r = ResultImage[x][y][1];
                }

                if (ResultImage[x][y][2] > max_g){
                    max_g = ResultImage[x][y][2];
                }
                if (ResultImage[x][y][2] < min_g){
                    min_g = ResultImage[x][y][2];
                }

                if (ResultImage[x][y][3] > max_b){
                    max_b = ResultImage[x][y][3];
                }               
                if (ResultImage[x][y][3] < min_b){
                    min_b = ResultImage[x][y][3];
                }
            }
        }

        for (int y=0; y<height; y++){
            for (int x=0; x<width; x++){
                if (max_r-min_r == 0){
                    ResultImage[x][y][1] = 0;
                } else {
                    ResultImage[x][y][1] = (255*(ResultImage[x][y][1]-min_r)) / (max_r-min_r);
                }

                if (max_g-min_g == 0){
                    ResultImage[x][y][2] = 0;
                } else {
                    ResultImage[x][y][2] = (255*(ResultImage[x][y][2]-min_g)) / (max_g-min_g);
                }

                if (max_b-min_b == 0){
                    ResultImage[x][y][3] = 0;
                } else {
                    ResultImage[x][y][3] = (255*(ResultImage[x][y][3]-min_b)) / (max_b-min_b);
                }
            }
        }

        return convertToBimage(ResultImage);
    }

    // Multiplication
    public BufferedImage multiply(BufferedImage img1, BufferedImage img2){
        int width = img1.getWidth();
        int height = img1.getHeight();

        int[][][] ImageArray1 = convertToArray(img1);
        int[][][] ImageArray2 = convertToArray(img2);
        int[][][] ResultImage = new int[width][height][4];

        int min_r = 255;
        int min_g = 255;
        int min_b = 255;
        int max_r = 0;
        int max_g = 0;
        int max_b = 0;

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ResultImage[x][y][1] = ImageArray1[x][y][1] * ImageArray2[x][y][1];
                ResultImage[x][y][2] = ImageArray1[x][y][2] * ImageArray2[x][y][2];
                ResultImage[x][y][3] = ImageArray1[x][y][3] * ImageArray2[x][y][3];

                if (ResultImage[x][y][1] > max_r){
                    max_r = ResultImage[x][y][1];
                }
                if (ResultImage[x][y][1] < min_r){
                    min_r = ResultImage[x][y][1];
                }

                if (ResultImage[x][y][2] > max_g){
                    max_g = ResultImage[x][y][2];
                }
                if (ResultImage[x][y][2] < min_g){
                    min_g = ResultImage[x][y][2];
                }

                if (ResultImage[x][y][3] > max_b){
                    max_b = ResultImage[x][y][3];
                }               
                if (ResultImage[x][y][3] < min_b){
                    min_b = ResultImage[x][y][3];
                }
            }
        }

        for (int y=0; y<height; y++){
            for (int x=0; x<width; x++){
                ResultImage[x][y][1] = (255*(ResultImage[x][y][1]-min_r)) / (max_r-min_r);
                ResultImage[x][y][2] = (255*(ResultImage[x][y][2]-min_g)) / (max_g-min_g);
                ResultImage[x][y][3] = (255*(ResultImage[x][y][3]-min_b)) / (max_b-min_b);
            }
        }
        return convertToBimage(ResultImage);
    }


    // Division
    public BufferedImage divide(BufferedImage img1, BufferedImage img2){
        int width = img1.getWidth();
        int height = img1.getHeight();

        int[][][] ImageArray1 = convertToArray(img1);
        int[][][] ImageArray2 = convertToArray(img2);
        int[][][] ResultImage = new int[width][height][4];

        int min_r = 255;
        int min_g = 255;
        int min_b = 255;
        int max_r = 0;
        int max_g = 0;
        int max_b = 0;

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                // check for divide by 0
                if (ImageArray2[x][y][1] == 0){
                    ImageArray2[x][y][1] = 1;
                }
                if (ImageArray2[x][y][2] == 0){
                    ImageArray2[x][y][2] = 1;
                }
                if (ImageArray2[x][y][3] == 0){
                    ImageArray2[x][y][3] = 1;
                }
                ResultImage[x][y][1] = ImageArray1[x][y][1] / ImageArray2[x][y][1];
                ResultImage[x][y][2] = ImageArray1[x][y][2] / ImageArray2[x][y][2];
                ResultImage[x][y][3] = ImageArray1[x][y][3] / ImageArray2[x][y][3];

                if (ResultImage[x][y][1] > max_r){
                    max_r = ResultImage[x][y][1];
                }
                if (ResultImage[x][y][1] < min_r){
                    min_r = ResultImage[x][y][1];
                }

                if (ResultImage[x][y][2] > max_g){
                    max_g = ResultImage[x][y][2];
                }
                if (ResultImage[x][y][2] < min_g){
                    min_g = ResultImage[x][y][2];
                }

                if (ResultImage[x][y][3] > max_b){
                    max_b = ResultImage[x][y][3];
                }               
                if (ResultImage[x][y][3] < min_b){
                    min_b = ResultImage[x][y][3];
                }
            }
        }

        for (int y=0; y<height; y++){
            for (int x=0; x<width; x++){
                ResultImage[x][y][1] = (255*(ResultImage[x][y][1]-min_r)) / (max_r-min_r);
                ResultImage[x][y][2] = (255*(ResultImage[x][y][2]-min_g)) / (max_g-min_g);
                ResultImage[x][y][3] = (255*(ResultImage[x][y][3]-min_b)) / (max_b-min_b);
            }
        }
        return convertToBimage(ResultImage);
    }

    // Lab 3 exercise 2
    // Bitwise NOT
    public BufferedImage bitwiseNOT(BufferedImage img){
        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = (~ImageArray[x][y][1])&0xFF;  //r
                ImageArray[x][y][2] = (~ImageArray[x][y][2])&0xFF;  //g
                ImageArray[x][y][3] = (~ImageArray[x][y][3])&0xFF;  //b
            }
        }
        return convertToBimage(ImageArray);
    }

    // Lab 3 exercise 3
    // Bitwise AND
    public BufferedImage bitwiseAND(BufferedImage img1, BufferedImage img2){
        int width = img1.getWidth();
        int height = img1.getHeight();

        int[][][] ImageArray1 = convertToArray(img1);
        int[][][] ImageArray2 = convertToArray(img2);
        int[][][] ResultImage = new int[width][height][4];

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ResultImage[x][y][1] = ImageArray1[x][y][1] & ImageArray2[x][y][1];
                ResultImage[x][y][2] = ImageArray1[x][y][2] & ImageArray2[x][y][2];
                ResultImage[x][y][3] = ImageArray1[x][y][3] & ImageArray2[x][y][3];
            }
        }
        return convertToBimage(ResultImage);
    }

    // Bitwise OR
    public BufferedImage bitwiseOR(BufferedImage img1, BufferedImage img2){
        int width = img1.getWidth();
        int height = img1.getHeight();

        int[][][] ImageArray1 = convertToArray(img1);
        int[][][] ImageArray2 = convertToArray(img2);
        int[][][] ResultImage = new int[width][height][4];

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ResultImage[x][y][1] = ImageArray1[x][y][1] | ImageArray2[x][y][1];
                ResultImage[x][y][2] = ImageArray1[x][y][2] | ImageArray2[x][y][2];
                ResultImage[x][y][3] = ImageArray1[x][y][3] | ImageArray2[x][y][3];
            }
        }
        return convertToBimage(ResultImage);
    }

    // Bitwise XOR
    public BufferedImage bitwiseXOR(BufferedImage img1, BufferedImage img2){
        int width = img1.getWidth();
        int height = img1.getHeight();

        int[][][] ImageArray1 = convertToArray(img1);
        int[][][] ImageArray2 = convertToArray(img2);
        int[][][] ResultImage = new int[width][height][4];

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ResultImage[x][y][1] = ImageArray1[x][y][1] ^ ImageArray2[x][y][1];
                ResultImage[x][y][2] = ImageArray1[x][y][2] ^ ImageArray2[x][y][2];
                ResultImage[x][y][3] = ImageArray1[x][y][3] ^ ImageArray2[x][y][3];
            }
        }
        return convertToBimage(ResultImage);
    }

    // Lab 4 exercise 1
    public BufferedImage MyNegative(BufferedImage img){
        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);          //  Convert the image to array

        // Image Negative Operation:
        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = 255-ImageArray[x][y][1];  //r
                ImageArray[x][y][2] = 255-ImageArray[x][y][2];  //g
                ImageArray[x][y][3] = 255-ImageArray[x][y][3];  //b
            }
        }
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    //Lab 4 exercise 2
    // Log Operator
    public BufferedImage LogarithmicOperator(BufferedImage img){
        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);          //  Convert the image to array

        double c = 255 / Math.log10(256);

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = (int)(c*Math.log10(1 + ImageArray[x][y][1]));  //r
                ImageArray[x][y][2] = (int)(c*Math.log10(1 + ImageArray[x][y][2]));  //g
                ImageArray[x][y][3] = (int)(c*Math.log10(1 + ImageArray[x][y][3]));  //b
            }
        }
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    //Lab 4 exercise 3
    // Power Law
    public BufferedImage PowerLaw(BufferedImage img, double p){
        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);          //  Convert the image to array

        double c = 255 / Math.pow(255, p);

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = (int)(c*Math.pow(ImageArray[x][y][1], p));  //r
                ImageArray[x][y][2] = (int)(c*Math.pow(ImageArray[x][y][2], p));  //g
                ImageArray[x][y][3] = (int)(c*Math.pow(ImageArray[x][y][3], p));  //b
            }
        }
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    //Lab 4 exercise 4
    // Random Look-Up Table
    public BufferedImage RandomLUT(BufferedImage img){
        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);
        int[] LUT = new int[256];

        Random rand = new Random();
        int upperBoundary = 255;

        for(int k=0; k<255; k++){
            LUT[k] = rand.nextInt(upperBoundary);
        }

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                ImageArray[x][y][1] = LUT[ImageArray[x][y][1]];  //r
                ImageArray[x][y][2] = LUT[ImageArray[x][y][2]];  //g
                ImageArray[x][y][3] = LUT[ImageArray[x][y][3]];  //b
            }
        }
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }

    //Lab 4 exercise 5
    //Bit-plane slicing
    public BufferedImage BitPlane(BufferedImage img, int k){
        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);

        int min_r = 255;
        int min_g = 255;
        int min_b = 255;
        int max_r = 0;
        int max_g = 0;
        int max_b = 0;

        for(int y=0; y<height; y++){
            for(int x=0; x<width; x++){
                ImageArray[x][y][1] = (ImageArray[x][y][1]>>k)&1;  //r
                ImageArray[x][y][2] = (ImageArray[x][y][2]>>k)&1;  //g
                ImageArray[x][y][3] = (ImageArray[x][y][3]>>k)&1;  //b

                if (ImageArray[x][y][1] > max_r){
                    max_r = ImageArray[x][y][1];
                }
                if (ImageArray[x][y][1] < min_r){
                    min_r = ImageArray[x][y][1];
                }

                if (ImageArray[x][y][2] > max_g){
                    max_g = ImageArray[x][y][2];
                }
                if (ImageArray[x][y][2] < min_g){
                    min_g = ImageArray[x][y][2];
                }

                if (ImageArray[x][y][3] > max_b){
                    max_b = ImageArray[x][y][3];
                }               
                if (ImageArray[x][y][3] < min_b){
                    min_b = ImageArray[x][y][3];
                }
            }
        }

        for (int y=0; y<height; y++){
            for (int x=0; x<width; x++){
                ImageArray[x][y][1] = (255*(ImageArray[x][y][1]-min_r)) / (max_r-min_r);
                ImageArray[x][y][2] = (255*(ImageArray[x][y][2]-min_g)) / (max_g-min_g);
                ImageArray[x][y][3] = (255*(ImageArray[x][y][3]-min_b)) / (max_b-min_b);
            }
        }
        return convertToBimage(ImageArray);  // Convert the array to BufferedImage
    }


    // Lab5 exercise 1
    // Finding histogram
    public BufferedImage apply_hist_equalisation(BufferedImage img){
        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);
        int[][][] ResultImage = new int[width][height][4];

        int[] histR = new int[256];
        int[] histG = new int[256];
        int[] histB = new int[256];

        double[] nhistR = new double[256];
        double[] nhistG = new double[256];
        double[] nhistB = new double[256];

        int[] LUT_histR = new int[256];
        int[] LUT_histG = new int[256];
        int[] LUT_histB = new int[256];

        for (int i=0; i<256; i++){
            histR[i] = 0;
            histG[i] = 0;
            histB[i] = 0;
        }

        // count pixel values
        for (int y=0; y<height; y++){
            for (int x=0; x<width; x++){
                int r = ImageArray[x][y][1];
                int g = ImageArray[x][y][2];
                int b = ImageArray[x][y][3];

                histR[r]++;
                histG[g]++;
                histB[b]++;
            }
        }

        // normalize histogram
        for (int i=0; i<255; i++){
            nhistR[i] = (double)histR[i] / (height*width);
            nhistG[i] = (double)histG[i] / (height*width);
            nhistB[i] = (double)histB[i] / (height*width);
        }
        
        double cum_r = 0;
        double cum_g = 0;
        double cum_b = 0;
        // Create lookup table/map function
        for (int i=0; i<256; i++){

            // cumulative distribution
            cum_r += nhistR[i];
            cum_g += nhistG[i];
            cum_b += nhistB[i];

            // multiply cumulative values
            LUT_histR[i] = (int)(cum_r*255);
            LUT_histG[i] = (int)(cum_g*255);
            LUT_histB[i] = (int)(cum_b*255);
        }

        for (int y=0; y<height; y++){
            for (int x=0; x<width; x++){
                int r = ImageArray[x][y][1];
                int g = ImageArray[x][y][2];
                int b = ImageArray[x][y][3];

                ResultImage[x][y][1] = LUT_histR[r];
                ResultImage[x][y][2] = LUT_histG[g];
                ResultImage[x][y][3] = LUT_histB[b];
            }
        }
        return convertToBimage(ResultImage);
    }

    public int[][] get_hist(BufferedImage img){
        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);

        int[] histR = new int[256];
        int[] histG = new int[256];
        int[] histB = new int[256];

        // count pixel values
        for (int y=0; y<height; y++){
            for (int x=0; x<width; x++){
                int r = ImageArray[x][y][1];
                int g = ImageArray[x][y][2];
                int b = ImageArray[x][y][3];

                histR[r]++;
                histG[g]++;
                histB[b]++;
            }
        }

        int[][] histogram = new int[3][256];
        histogram[0] = histR;
        histogram[1] = histG;
        histogram[2] = histB;

        return histogram;
    }

    // Lab6 exercise 1
    // Convolution
    public BufferedImage convolve(BufferedImage img, double[][] mask){
        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);
        int[][][] ResultImage = new int[width][height][4];

        int kernel_size = mask[0].length;

        int min_r = 255;
        int min_g = 255;
        int min_b = 255;
        int max_r = 0;
        int max_g = 0;
        int max_b = 0;

        for (int y=kernel_size-2; y<height-1; y++){
            for (int x=kernel_size-2; x<width-1; x++){
                double r=0;
                double g=0;
                double b=0;

                for (int s=-(kernel_size-2); s<=1; s++){
                    for(int t=-(kernel_size-2); t<=1; t++){
                        r = r + mask[1-s][1-t]*ImageArray[x+s][y+t][1];
                        g = g + mask[1-s][1-t]*ImageArray[x+s][y+t][2];
                        b = b + mask[1-s][1-t]*ImageArray[x+s][y+t][3];
                    }
                }
        
                ResultImage[x][y][1] = Math.abs((int)r);
                ResultImage[x][y][2] = Math.abs((int)g);
                ResultImage[x][y][3] = Math.abs((int)b);

                if (ResultImage[x][y][1] > max_r){
                    max_r = ResultImage[x][y][1];
                }
                if (ResultImage[x][y][1] < min_r){
                    min_r = ResultImage[x][y][1];
                }

                if (ResultImage[x][y][2] > max_g){
                    max_g = ResultImage[x][y][2];
                }
                if (ResultImage[x][y][2] < min_g){
                    min_g = ResultImage[x][y][2];
                }

                if (ResultImage[x][y][3] > max_b){
                    max_b = ResultImage[x][y][3];
                }               
                if (ResultImage[x][y][3] < min_b){
                    min_b = ResultImage[x][y][3];
                }
            }
        }

        for (int y=0; y<height; y++){
            for (int x=0; x<width; x++){
                ResultImage[x][y][1] = (255*(ResultImage[x][y][1]-min_r)) / (max_r-min_r);
                ResultImage[x][y][2] = (255*(ResultImage[x][y][2]-min_g)) / (max_g-min_g);
                ResultImage[x][y][3] = (255*(ResultImage[x][y][3]-min_b)) / (max_b-min_b);
            }
        }
        return convertToBimage(ResultImage);
    }

    // Lab7 exercise 1
    // Salt-and-Pepper Noise
    public BufferedImage add_salt_pepper(BufferedImage img, double p){
        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);          //  Convert the image to array

        Random rand = new Random();

        for(int y=0; y<height; y++){
            for(int x =0; x<width; x++){
                double random = rand.nextDouble();

                // 50% chance 
                if (random < p/2) {
                    ImageArray[x][y][1] = 255;  //r
                    ImageArray[x][y][2] = 255;  //g
                    ImageArray[x][y][3] = 255;  //b
                }
                else if (random < p) {
                    ImageArray[x][y][1] = 0;  //r
                    ImageArray[x][y][2] = 0;  //g
                    ImageArray[x][y][3] = 0;  //b
                }
            }
        }
        return convertToBimage(ImageArray);
    }

    // Lab7 exercise 2
    // Min Filter
    public BufferedImage minFilter(BufferedImage img, int windowSize){
        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);
        int[][][] ResultImage = new int[width][height][4];

        for (int y=windowSize-2; y<height-1; y++){
            for (int x=windowSize-2; x<width-1; x++){
                int min_r = 255;
                int min_g = 255;
                int min_b = 255;

                for (int s=-(windowSize-2); s<=1; s++){
                    for(int t=-(windowSize-2); t<=1; t++){
                        int r = ImageArray[x+s][y+t][1];
                        int g = ImageArray[x+s][y+t][2];
                        int b = ImageArray[x+s][y+t][3];

                        if (r < min_r){
                            min_r = r;
                        }
                        if (g < min_g){
                            min_g = g;
                        }              
                        if (b < min_b){
                            min_b = b;
                        }
                    }
                }
                ResultImage[x][y][1] = min_r;
                ResultImage[x][y][2] = min_g;
                ResultImage[x][y][3] = min_b;
            }
        }
        return convertToBimage(ResultImage);
    }


    // Lab7 exercise 3
    // Max Filter
    public BufferedImage maxFilter(BufferedImage img, int windowSize){
        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);
        int[][][] ResultImage = new int[width][height][4];

        for (int y=windowSize-2; y<height-1; y++){
            for (int x=windowSize-2; x<width-1; x++){
                int max_r = 0;
                int max_g = 0;
                int max_b = 0;

                for (int s=-(windowSize-2); s<=1; s++){
                    for(int t=-(windowSize-2); t<=1; t++){
                        int r = ImageArray[x+s][y+t][1];
                        int g = ImageArray[x+s][y+t][2];
                        int b = ImageArray[x+s][y+t][3];

                        if (r > max_r){
                            max_r = r;
                        }
                        if (g > max_g){
                            max_g = g;
                        }              
                        if (b > max_b){
                            max_b = b;
                        }
                    }
                }
                ResultImage[x][y][1] = max_r;
                ResultImage[x][y][2] = max_g;
                ResultImage[x][y][3] = max_b;
            }
        }
        return convertToBimage(ResultImage);
    }

    // Lab7 exercise 4
    // Mid-Point Filter
    public BufferedImage midPointFilter(BufferedImage img, int windowSize){
        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);
        int[][][] ResultImage = new int[width][height][4];

        for (int y=windowSize-2; y<height-1; y++){
            for (int x=windowSize-2; x<width-1; x++){
                int min_r = 255;
                int min_g = 255;
                int min_b = 255;
                int max_r = 0;
                int max_g = 0;
                int max_b = 0;

                for (int s=-(windowSize-2); s<=1; s++){
                    for(int t=-(windowSize-2); t<=1; t++){
                        int r = ImageArray[x+s][y+t][1];
                        int g = ImageArray[x+s][y+t][2];
                        int b = ImageArray[x+s][y+t][3];

                        if (r > max_r){
                            max_r = r;
                        }
                        if (g > max_g){
                            max_g = g;
                        }              
                        if (b > max_b){
                            max_b = b;
                        }

                        if (r < min_r){
                            min_r = r;
                        }
                        if (g < min_g){
                            min_g = g;
                        }              
                        if (b < min_b){
                            min_b = b;
                        }
                    }
                }
                ResultImage[x][y][1] = (max_r + min_r)/2;
                ResultImage[x][y][2] = (max_g + min_g)/2;
                ResultImage[x][y][3] = (max_b + min_b)/2;
            }
        }
        return convertToBimage(ResultImage);
    }

    // Lab7 exercise 5
    // Median Filter
    public BufferedImage medianFilter(BufferedImage img, int windowSize){
        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);
        int[][][] ResultImage = new int[width][height][4];

        int[] rWindow = new int[windowSize*windowSize];
        int[] gWindow = new int[windowSize*windowSize];
        int[] bWindow = new int[windowSize*windowSize];

        for (int y=windowSize-2; y<height-1; y++){
            for (int x=windowSize-2; x<width-1; x++){
                
                int k = 0;

                for (int s=-(windowSize-2); s<=1; s++){
                    for(int t=-(windowSize-2); t<=1; t++){
                        rWindow[k] = ImageArray[x+s][y+t][1];
                        gWindow[k] = ImageArray[x+s][y+t][2];
                        bWindow[k] = ImageArray[x+s][y+t][3];
                        k++;
                    }
                }
                Arrays.sort(rWindow);
                Arrays.sort(gWindow);
                Arrays.sort(bWindow);
                ResultImage[x][y][1] = rWindow[(int)k/2];
                ResultImage[x][y][2] = gWindow[(int)k/2];
                ResultImage[x][y][3] = bWindow[(int)k/2];
            }
        } 
        return convertToBimage(ResultImage);
    }

    // Lab8 exercise 1
    // Mean from histogram
    public int[] compute_mean(BufferedImage img){
        int width = img.getWidth();
        int height = img.getHeight();

        int[][] histogram = get_hist(img);

        int[] result = new int[3];
        result[0] = 0;
        result[1] = 0;
        result[2] = 0;

        for (int i=0; i<256; i++){
            result[0] += histogram[0][i]*i;
            result[1] += histogram[1][i]*i;
            result[2] += histogram[2][i]*i;
        } 

        result[0] = (int)(result[0] / (height*width));
        result[1] = (int)(result[1] / (height*width));
        result[2] = (int)(result[2] / (height*width));
        return result;
    }

    // Standard deviation from histogram
    public double[] compute_std(BufferedImage img){
        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);

        int[] mean = compute_mean(img);
        double[] result = new double[3];
        result[0] = 0;
        result[1] = 0;
        result[2] = 0;

        for (int y=0; y<height; y++){
            for (int x=0; x<width; x++){
                result[0] += Math.pow((ImageArray[x][y][1]-mean[0]),2);
                result[1] += Math.pow((ImageArray[x][y][2]-mean[1]),2);
                result[2] += Math.pow((ImageArray[x][y][3]-mean[2]),2);
            }
        }
    
        result[0] = Math.sqrt(result[0] / (height*width));
        result[1] = Math.sqrt(result[1] / (height*width));
        result[2] = Math.sqrt(result[2] / (height*width));
        return result;
    }

    // Lab8 exercise 2
    // Simple Thresholding
    public BufferedImage simple_thresholding(BufferedImage img, int threshold){
        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);
        int[][][] ResultImage = new int[width][height][4];

        for (int y=0; y<height; y++){
            for (int x=0; x<width; x++){

                if (ImageArray[x][y][1] < threshold){
                    ResultImage[x][y][1] = 0;
                }
                if (ImageArray[x][y][2] < threshold){
                    ResultImage[x][y][2] = 0;
                }
                if (ImageArray[x][y][3] < threshold){
                    ResultImage[x][y][3] = 0;
                }
                if (ImageArray[x][y][1] >= threshold){
                    ResultImage[x][y][1] = 255;
                }
                if (ImageArray[x][y][2] >= threshold){
                    ResultImage[x][y][2] = 255;
                }
                if (ImageArray[x][y][3] >= threshold){
                    ResultImage[x][y][3] = 255;
                }
            }
        }
        return convertToBimage(ResultImage);
    }

    public static double mean(double[] p) {
        double sum = 0;  // sum of all the elements
        for (int i=0; i<p.length; i++) {
            sum += p[i];
        }
        return sum / p.length;
    }

    // Lab8 exercise 3
    // Automated Thresholding
    public BufferedImage automated_thresholding(BufferedImage img, int automated_threshold){
        int width = img.getWidth();
        int height = img.getHeight();

        int[][][] ImageArray = convertToArray(img);
        int[][][] ResultImage = new int[width][height][4];

        double[] background = new double[3];
        double[] object = new double[3];

        int background_pixels = 4;
        int object_pixels = (width*height)-4;

        background[0] = 0;
        background[1] = 0;
        background[2] = 0;

        object[0] = 0;
        object[1] = 0;
        object[2] = 0;

        double threshold;
        double new_threshold = 255;

        for (int y=0; y<height; y++){
            for (int x=0; x<width; x++){
                if (x==0 && y==0){
                    background[0] += ImageArray[x][y][1];
                    background[1] += ImageArray[x][y][2];
                    background[2] += ImageArray[x][y][3];
                    continue;
                }
                if (x==0 && y==height-1){
                    background[0] += ImageArray[x][y][1];
                    background[1] += ImageArray[x][y][2];
                    background[2] += ImageArray[x][y][3];
                    continue;
                }
                if (x==width-1 && y==0){
                    background[0] += ImageArray[x][y][1];
                    background[1] += ImageArray[x][y][2];
                    background[2] += ImageArray[x][y][3];
                    continue;
                }
                if (x==width-1 && y==height-1){
                    background[0] += ImageArray[x][y][1];
                    background[1] += ImageArray[x][y][2];
                    background[2] += ImageArray[x][y][3];
                    continue;
                }
                object[0] += ImageArray[x][y][1];
                object[1] += ImageArray[x][y][2];
                object[2] += ImageArray[x][y][3];
            }
        }

        background[0] = background[0] / background_pixels;
        background[1] = background[1] / background_pixels;
        background[2] = background[2] / background_pixels;
        object[0] = object[0] / object_pixels;
        object[1] = object[1] / object_pixels;
        object[2] = object[2] / object_pixels;

        
        System.out.println("Background 0: " + mean(background));
        System.out.println("Object 0: " + mean(object));
        threshold = (mean(background) + mean(object)) / 2;

        while (Math.abs(new_threshold - threshold) > automated_threshold){
            threshold = (mean(background) + mean(object)) / 2;
            background_pixels = 0;
            object_pixels = 0;

            for (int y=0; y<height; y++){
                for (int x=0; x<width; x++){
    
                    if (ImageArray[x][y][1] < threshold){
                        ResultImage[x][y][1] = 0;
                        background[0] += ResultImage[x][y][1];
                        background_pixels += 1;
                    }
                    if (ImageArray[x][y][2] < threshold){
                        ResultImage[x][y][2] = 0;
                        background[1] += ResultImage[x][y][2];
                        background_pixels += 1;
                    }
                    if (ImageArray[x][y][3] < threshold){
                        ResultImage[x][y][3] = 0;
                        background[2] += ResultImage[x][y][3];
                        background_pixels += 1;
                    }
                    if (ImageArray[x][y][1] >= threshold){
                        ResultImage[x][y][1] = 255;
                        object[0] += ResultImage[x][y][1];
                        object_pixels += 1;
                    }
                    if (ImageArray[x][y][2] >= threshold){
                        ResultImage[x][y][2] = 255;
                        object[1] += ResultImage[x][y][2];
                        object_pixels += 1;
                    }
                    if (ImageArray[x][y][3] >= threshold){
                        ResultImage[x][y][3] = 255;
                        object[2] += ResultImage[x][y][3];
                        object_pixels += 1;
                    }
                }
            }

            background[0] = background[0] / background_pixels;
            background[1] = background[1] / background_pixels;
            background[2] = background[2] / background_pixels;
            object[0] = object[0] / object_pixels;
            object[1] = object[1] / object_pixels;
            object[2] = object[2] / object_pixels;

            System.out.println("Background: " + mean(background));
            System.out.println("Object: " + mean(object));
            new_threshold = (mean(background) + mean(object)) / 2;;
            System.out.println("Threshold: " + threshold);
            System.out.println("New Threshold: " + new_threshold);
        }
        return convertToBimage(ResultImage);
    }

    //************************************


    


    //************************************
    //  You need to register your functioin here
    //************************************
    public void filterImage() {
 
        if (opIndex == lastOp) {
            return;
        }

        lastOp = opIndex;
        switch (opIndex) {
        case 0: 
                lastImage = getLastImage();
                biFiltered = bi; /* original */
                return; 
        case 1: 
                lastImage = getLastImage();
                biFiltered = ImageNegative(bi); /* Image Negative */
                return;
        //************************************
        case 2: 
                lastImage = getLastImage();
                //biFiltered = TwoImages(image_1, image_2);
                displaySecondImage(image_2);
                return;
        case 3:
                lastImage = getLastImage();
                biFiltered = rescale(biFiltered, scale_value);
                return;
        case 4:
                lastImage = getLastImage();
                biFiltered = shift(biFiltered, shift_value);
                return;
        case 5:
                lastImage = getLastImage();
                biFiltered = random_shift(biFiltered);
                return;
        case 6:
                lastImage = getLastImage();
                try {
                    BufferedImage img2 = ImageIO.read(new File(image_2));
                    biFiltered = add(biFiltered, img2);
                } catch (IOException e) {
                    System.out.println("Image could not be read");
                }
                return;
        case 7:
                lastImage = getLastImage();
                try {
                    BufferedImage img2 = ImageIO.read(new File(image_2));
                    biFiltered = subtract(biFiltered, img2);
                } catch (IOException e) {
                    System.out.println("Image could not be read");
                }
                return;
        case 8:
                lastImage = getLastImage();
                try {
                    BufferedImage img2 = ImageIO.read(new File(image_2));
                    biFiltered = multiply(biFiltered, img2);
                } catch (IOException e) {
                    System.out.println("Image could not be read");
                }
                return;
        case 9:
                lastImage = getLastImage();
                try {
                    BufferedImage img2 = ImageIO.read(new File(image_2));
                    biFiltered = divide(biFiltered, img2);
                } catch (IOException e) {
                    System.out.println("Image could not be read");
                }
                return;
        case 10:
                lastImage = getLastImage();
                biFiltered = bitwiseNOT(biFiltered);
                return;
        case 11:
                lastImage = getLastImage();
                try {
                    BufferedImage img2 = ImageIO.read(new File(image_2));
                    biFiltered = bitwiseAND(biFiltered, img2);
                } catch (IOException e) {
                    System.out.println("Image could not be read");
                }
                return;
        case 12:
                lastImage = getLastImage();
                try {
                    BufferedImage img2 = ImageIO.read(new File(image_2));
                    biFiltered = bitwiseOR(biFiltered, img2);
                } catch (IOException e) {
                    System.out.println("Image could not be read");
                }
                return;
        case 13:
                lastImage = getLastImage();
                try {
                    BufferedImage img2 = ImageIO.read(new File(image_2));
                    biFiltered = bitwiseXOR(biFiltered, img2);
                } catch (IOException e) {
                    System.out.println("Image could not be read");
                }
                return;
        case 14:
                lastImage = getLastImage();
                biFiltered = MyNegative(biFiltered);
                return;
        case 15:
                lastImage = getLastImage();
                biFiltered = LogarithmicOperator(biFiltered);
                return;
        case 16:
                lastImage = getLastImage();
                biFiltered = PowerLaw(biFiltered, power);
                return;
        case 17:
                lastImage = getLastImage();
                biFiltered = RandomLUT(biFiltered);
                return;
        case 18:
                lastImage = getLastImage();
                biFiltered = BitPlane(biFiltered, kbit);
                return;
        case 19:
                lastImage = getLastImage();
                biFiltered = apply_hist_equalisation(biFiltered);
                return;
        case 20:
                lastImage = getLastImage();
                get_hist(biFiltered);
                return;
        case 21:
                lastImage = getLastImage();
                biFiltered = convolve(biFiltered, mean_mask);
                return;
        case 22:
                lastImage = getLastImage();
                biFiltered = convolve(biFiltered, weighted_mean_mask);
                return;
        case 23:
                lastImage = getLastImage();
                biFiltered = convolve(biFiltered, Laplacian_4);
                return;
        case 24:
                lastImage = getLastImage();
                biFiltered = convolve(biFiltered, Laplacian_8);
                return;
        case 25:
                lastImage = getLastImage();
                biFiltered = convolve(biFiltered, LaplacianEnhancement_4);
                return;
        case 26:
                lastImage = getLastImage();
                biFiltered = convolve(biFiltered, LaplacianEnhancement_8);
                return;
        case 27:
                lastImage = getLastImage();
                biFiltered = convolve(biFiltered, Roberts_1);
                return;
        case 28:
                lastImage = getLastImage();
                biFiltered = convolve(biFiltered, Roberts_2);
                return;
        case 29:
                lastImage = getLastImage();
                biFiltered = convolve(biFiltered, Sobel_X);
                return;
        case 30:
                lastImage = getLastImage();
                biFiltered = convolve(biFiltered, Sobel_Y);
                return;
        case 31:
                lastImage = getLastImage();
                biFiltered = convolve(biFiltered, Gaussian);
                return;
        case 32:
                lastImage = getLastImage();
                biFiltered = convolve(biFiltered, LoG);
                return;
        case 33:
                lastImage = getLastImage();
                biFiltered = add_salt_pepper(biFiltered, probability);
                return;
        case 34:
                lastImage = getLastImage();
                biFiltered = minFilter(biFiltered, minFilterWindow);
                return;
        case 35:
                lastImage = getLastImage();
                biFiltered = maxFilter(biFiltered, maxFilterWindow);
                return;
        case 36:
                lastImage = getLastImage();
                biFiltered = midPointFilter(biFiltered, midPointFilterWindow);
                return;
        case 37:
                lastImage = getLastImage();
                biFiltered = medianFilter(biFiltered, midPointFilterWindow);
                return;
        case 38:
                int[] mean = compute_mean(biFiltered);
                System.out.println(mean[0]);
                System.out.println(mean[1]);
                System.out.println(mean[2]);
                return;
        case 39:
                double[] std = compute_std(biFiltered);
                System.out.println(std[0]);
                System.out.println(std[1]);
                System.out.println(std[2]);
                return;
        case 40:
                lastImage = getLastImage();
                biFiltered = simple_thresholding(biFiltered, threshold);
                return;
        case 41:
                lastImage = getLastImage();
                biFiltered = automated_thresholding(biFiltered, automated_threshold);
                return;
        //************************************

        }
    }
 

 
     public void actionPerformed(ActionEvent e) {
        // JComboBox cb = (JComboBox)e.getSource();

         
        //  if (cb.getActionCommand().equals("SetFilter")) {
        //      setOpIndex(cb.getSelectedIndex());
        //      repaint();
        //  } else if (cb.getActionCommand().equals("Formats")) {
        //      String format = (String)cb.getSelectedItem();
        //      File saveFile = new File("savedimage."+format);
        //      JFileChooser chooser = new JFileChooser();
        //      chooser.setSelectedFile(saveFile);
        //      int rval = chooser.showSaveDialog(cb);
        //      if (rval == JFileChooser.APPROVE_OPTION) {
        //          saveFile = chooser.getSelectedFile();
        //          try {
        //              ImageIO.write(biFiltered, format, saveFile);
        //          } catch (IOException ex) {
        //          }
        //      }
        //  }
        Object source = e.getSource();

        if (source instanceof JComboBox){
            JComboBox cb = (JComboBox)e.getSource();
            if (cb.getActionCommand().equals("SetFilter")) {
                setOpIndex(cb.getSelectedIndex());
                repaint();
            } else if (cb.getActionCommand().equals("Formats")) {
                String format = (String)cb.getSelectedItem();
                File saveFile = new File("savedimage."+format);
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(saveFile);
                int rval = chooser.showSaveDialog(cb);
                if (rval == JFileChooser.APPROVE_OPTION) {
                    saveFile = chooser.getSelectedFile();
                    try {
                        ImageIO.write(biFiltered, format, saveFile);
                    } catch (IOException ex) {
                    }
                }
            }
        } else if (source instanceof JButton){
            JButton b = (JButton)e.getSource();
            if (b.getActionCommand().equals("Undo")){
                biFiltered = lastImage;
                repaint();
            }
        }
    
    };

 
    public static void main(String s[]) {
        JFrame f = new JFrame("Image Processing Demo");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        });
        Demo de = new Demo(image_1);
        f.add("Center", de);
        JButton b = new JButton("Undo");
        b.setActionCommand("Undo");
        b.addActionListener(de);
        JComboBox choices = new JComboBox(de.getDescriptions());
        choices.setActionCommand("SetFilter");
        choices.addActionListener(de);
        JComboBox formats = new JComboBox(de.getFormats());
        formats.setActionCommand("Formats");
        formats.addActionListener(de);
        JPanel panel = new JPanel();
        panel.add(b);
        panel.add(choices);
        panel.add(new JLabel("Save As"));
        panel.add(formats);
        f.add("North", panel);
        f.pack();
        f.setVisible(true);
    }
}
