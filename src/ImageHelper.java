import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Kernel;
import java.io.*;
import java.util.Random;
import java.util.Vector;

public class ImageHelper {

    static BufferedImage image;
    static Snake ms;
    static Board mb;
    static int fx;
    static int fy;
    public static Vector<Double> iputFeatures = new Vector<>();
    public static Vector<Double> targetValues = new Vector<>();

    private static int sampleSize = 6;

    public static void saveImage(Snake s, Board b,int food_x,int food_y)
    {


        ms=s;
        mb=b;
        fx=food_x;
        fy=food_y;
        image = new BufferedImage(mb.getWidth(),mb.getHeight(),BufferedImage.TYPE_INT_ARGB);

        for(int i=0;i<mb.getWidth();i++)
        {
            for(int j=0;j<mb.getHeight();j++)
            {
                Color c;
                boolean filled = false;
                if(i >= mb.getWidth()-20 || i<=20 || j>= mb.getHeight()-20 || j<=20)
                {
                    c = new Color((255<<24 | 255<<16 | 255<<8 | 255));
                    image.setRGB(i,j,c.getRGB());
                    filled = true;
                }
                if(i >= fx && i<=fx+20 && j>= fy && j<=fy+20)
                {
                    c = new Color((255<<24 | 255<<16 | 0<<8 | 0));
                    image.setRGB(i,j,c.getRGB());
                    filled = true;
                }
                for(int k=0;k<ms.x.length;k++)
                {
                    if(i>=ms.x[k] && i<=ms.x[k]+20 && j>=ms.y[k] && j<=ms.y[k]+20)
                    {
                        if(k==0 || k==1 || k==2) {
                            c = new Color(255 << 24 | 0 << 16 | 0 << 8 | 255);
                            image.setRGB(i, j, c.getRGB());
                            filled = true;
                        }
                        else
                        {
                            c = new Color(255 << 24 | 0 << 16 | 255 << 8 | 0);
                            image.setRGB(i, j, c.getRGB());
                            filled = true;
                        }
                    }
                }
                if(!filled)
                {
                    c = new Color(0<<24 | 0<<16 | 0<<8 | 0);
                    image.setRGB(i,j,c.getRGB());
                }

            }
        }


        String str = new Random().nextInt(100000000)+ (ms.direction == Direction.right ? "_r" : ms.direction == Direction.left ? "_l" : ms.direction == Direction.up ? "_u" : "_d")+".png";
        new File("./src/images/").mkdir();
        File f = new File("./src/images/"+str);
        try {
            ImageIO.write(image,"png",f);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static double[][][] filters = new double[10][7][7];


    private static void randomizeFilters()
    {
        for(int i=0;i<filters.length;i++)
            for(int j=0;j<filters[i].length;j++)
                for(int k=0;k<filters[i][j].length;k++)
                    filters[i][j][k] = new Random().nextInt()%2 == 0 ? new Random().nextDouble() : new Random().nextDouble()*-1;
    }

    static FileWriter fw;

    public static void writeFeatures(String path) throws IOException {
        randomizeFilters();
        fw = new FileWriter(new File("./src/data.txt"));
        long index=new File("./src/images/").listFiles().length;

        for (File f : new File("./src/images/").listFiles()) {

           BufferedImage image =  ImageIO.read(f);
           iputFeatures.clear();
           targetValues.clear();

            targetValues.add(f.getName().contains("r") ? 1.0 : 0.0);
            targetValues.add(f.getName().contains("u") ? 1.0 : 0.0);
            targetValues.add(f.getName().contains("l") ? 1.0 : 0.0);
            targetValues.add(f.getName().contains("d") ? 1.0 : 0.0);

            recFilters(image,filters,iputFeatures);

            System.out.println("inputSize: "+iputFeatures.size());
            System.out.println("targetSize: "+targetValues.size());
            System.out.println("index: "+index--);
            for(int i=0;i<iputFeatures.size();i++)
                fw.append(String.valueOf(iputFeatures.get(i))+" ");
            fw.append('\n');
            for(int i=0;i<targetValues.size();i++)
                fw.append(String.valueOf(targetValues.get(i))+" ");
            fw.append('\n');


        }
        fw.close();
    }



    private static void recFilters(BufferedImage img,double[][][] filters , Vector<Double> ivector)
    {
        for(int i=0;i<filters.length;i++)
        {
            BufferedImage image = getFiltered(img,filters[i]);
            image = maxpool(image,sampleSize);

            if(image.getWidth()<sampleSize) {
                image = maxpool(image,image.getWidth()-1);
                double val = (double)new Color(image.getRGB(0, 0)).getRed();
                val /= 255.0;
                //System.out.println("RED :: "+new Color(image.getRGB(0, 0)).getRed());
                ivector.add(val);
                val = + (double)new Color(image.getRGB(0, 0)).getGreen();
                val/=255.0;
                //System.out.println("GREEN :: "+new Color(image.getRGB(0, 0)).getGreen());
                ivector.add(val);
                val = (double)new Color(image.getRGB(0, 0)).getBlue();
                val/=255.0;
                //System.out.println("BLUE :: "+new Color(image.getRGB(0, 0)).getBlue());
                ivector.add(val);
            }
            else {
                recFilters(image, filters,ivector);

            }
        }


    }

    public static Vector<Double> getFeatureVectors(BufferedImage img)
    {

        Vector<Double> ivector = new Vector<>();
        recFilters(img,filters,ivector);

        return ivector;

    }

    public static Vector<Double> getFeatureVectors(Snake s, Board b,int food_x,int food_y)
    {


        ms=s;
        mb=b;
        fx=food_x;
        fy=food_y;
        image = new BufferedImage(mb.getWidth(),mb.getHeight(),BufferedImage.TYPE_INT_ARGB);

        for(int i=0;i<mb.getWidth();i++)
        {
            for(int j=0;j<mb.getHeight();j++)
            {
                Color c;
                boolean filled = false;
                if(i >= mb.getWidth()-20 || i<=20 || j>= mb.getHeight()-20 || j<=20)
                {
                    c = new Color((255<<24 | 255<<16 | 255<<8 | 255));
                    image.setRGB(i,j,c.getRGB());
                    filled = true;
                }
                if(i >= fx && i<=fx+20 && j>= fy && j<=fy+20)
                {
                    c = new Color((255<<24 | 255<<16 | 0<<8 | 0));
                    image.setRGB(i,j,c.getRGB());
                    filled = true;
                }
                for(int k=0;k<ms.x.length;k++)
                {
                    if(i>=ms.x[k] && i<=ms.x[k]+20 && j>=ms.y[k] && j<=ms.y[k]+20)
                    {
                        if(k==0 || k==1 || k==2) {
                            c = new Color(255 << 24 | 0 << 16 | 0 << 8 | 255);
                            image.setRGB(i, j, c.getRGB());
                            filled = true;
                        }
                        else
                        {
                            c = new Color(255 << 24 | 0 << 16 | 255 << 8 | 0);
                            image.setRGB(i, j, c.getRGB());
                            filled = true;
                        }
                    }
                }
                if(!filled)
                {
                    c = new Color(0<<24 | 0<<16 | 0<<8 | 0);
                    image.setRGB(i,j,c.getRGB());
                }

            }
        }

        Vector<Double> ivector = new Vector<>();
        recFilters(image,filters,ivector);

        return ivector;

    }

    private static BufferedImage maxpool(BufferedImage img,int sz) {

        int size = sz;
        BufferedImage rtn = new BufferedImage(img.getWidth()/size,img.getHeight()/size,BufferedImage.TYPE_INT_ARGB);

        for(int i=0;i<img.getWidth()-size;i+=size)
        {
            for(int j=0;j<img.getHeight()-size;j+=size)
            {

                float valr = 0, valg = 0,valb=0;

                int maxindexx = i,maxindexy = j;
                for(int k=0;k<size;k++)
                {
                    for(int l=0;l<size;l++)
                    {
                        if(new Color(img.getRGB(i+k,j+l)).getRed()>new Color(img.getRGB(maxindexx,maxindexy)).getRed() ||
                                new Color(img.getRGB(i+k,j+l)).getGreen()>new Color(img.getRGB(maxindexx,maxindexy)).getGreen() ||
                                new Color(img.getRGB(i+k,j+l)).getBlue()>new Color(img.getRGB(maxindexx,maxindexy)).getBlue())
                        {
                            maxindexx = i+k;
                            maxindexy=i+k;
                        }

                    }
                }


                //System.out.println("MAXPOOL RED :"+ new Color(img.getRGB(maxindexx,maxindexy)).getRed());
                //System.out.println("MAXPOOL GREEN :" +new Color(img.getRGB(maxindexx,maxindexy)).getRed());
                //System.out.println("MAXPOOL BLUE :" +new Color(img.getRGB(maxindexx,maxindexy)).getRed());
                //rtn.setRGB(i/size,j/size,255<<24 | new Float(valr).intValue()<<16 | new Float(valg).intValue()<<8 | new Float(valb).intValue());

                rtn.setRGB(i/size,j/size,img.getRGB(maxindexx,maxindexy));

            }
        }

        return rtn;
    }

    private static BufferedImage getFiltered(BufferedImage img, double[][] filter) {

        BufferedImage tmp = new BufferedImage(img.getWidth()+filter.length-1,img.getHeight()+filter[0].length-1,BufferedImage.TYPE_INT_ARGB);

        for(int i=0;i<img.getWidth();i++)
            for(int j=0;j<img.getHeight();j++)
            {
                tmp.setRGB(i+((filter.length-1)/2),j+((filter[0].length-1)/2),img.getRGB(i,j));

            }

        for(int i=0;i<tmp.getWidth();i++)
            for(int j=0;j<tmp.getHeight();j++)
            {
                if(i< ((filter.length-1)/2) || i> tmp.getWidth()-((filter.length-1)/2) || j<((filter[0].length-1)/2) || j> tmp.getHeight()-((filter[0].length-1)/2))
                    tmp.setRGB(i,j,0<<24 | 0<<16 | 0<<8 | 0);

            }


        Random r = new Random();
        for(int i=0;i<tmp.getWidth()-filter.length;i++) {
            for (int j = 0; j < tmp.getHeight()-filter[0].length; j++) {

                int sumr=0 , sumg =0, sumb=0,rgb=0;
                for(int k=0;k<filter.length;k++) {
                    for (int l = 0; l < filter[k].length; l++) {

                        rgb += tmp.getRGB(i+k,j+l) * filter[k][l];
                        sumr += new Color(tmp.getRGB(i+k,j+l)).getRed()*filter[k][l];
                        sumg += new Color(tmp.getRGB(i+k,j+l)).getGreen()*filter[k][l];
                        sumb += new Color(tmp.getRGB(i+k,j+l)).getBlue()*filter[k][l];

                        //System.out.println("FARKLI");

                    }

                }


                tmp.setRGB(i,j,255<<24 | sumr<<16 | sumg<<8 | sumb);
            }

        }
        return tmp;
    }

}
