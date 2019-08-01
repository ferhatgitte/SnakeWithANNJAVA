import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Circle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Timer;


class customTTask extends TimerTask{

    Board b;
    public customTTask(Board board)
    {
        this.b = board;
    }
    @Override
    public void run() {

        b.scheduled();

    }
}

public class Board extends JFrame implements KeyListener{
    public Snake mSnake;
    private Timer timer;
    private int w = 300 ,h=300;
    private int food_x,food_y;
    private boolean alive = true;
    private JPanel panel;

    public void scheduled()
    {

        //ImageHelper.saveImage(mSnake,this,food_x,food_y);
        if(alive) {
            doDraw();
            mSnake.setDirection(m_Net.feedAndGetDir(ImageHelper.getFeatureVectors(mSnake,this,food_x,food_y)));
            mSnake.move();
            for(int i=3;i<mSnake.x.length;i++)
            {
                if(mSnake.x[0] == mSnake.x[i] && mSnake.y[0] == mSnake.y[i] )
                    alive=false;

            }


            if (mSnake.x[0] == food_x && mSnake.y[0] == food_y) {
                mSnake.grow();
                food_x = (new Random().nextInt(25) * 10)+10;
                food_y = (new Random().nextInt(25) * 10)+10;
            }

        }


    }

    NN m_Net;
    public Board(){

        super("Snake Game");
        addKeyListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        this.setSize(w,h);
        this.setVisible(true);
        init();
        Vector<Integer> tplgy = new Vector<>();
        tplgy.add(3000);
        tplgy.add(400);
        tplgy.add(200);
        tplgy.add(4);

        //try {
          //ImageHelper.writeFeatures("");
        //} catch (IOException e) {
          //e.printStackTrace();
        //}

        m_Net = NN.existingNN();
        if(m_Net == null) {
            m_Net = new NN(tplgy, "./src/data.txt");
            m_Net.init();
            m_Net.train();
        }




        timer = new Timer();

        timer.schedule(new customTTask(this),0,200);

    }

    private int cellSize = 10;
    private void doDraw() {

        panel.removeAll();

        for(int i=0;i<w;i+=cellSize)
        {
            for(int j=0;j<h;j+=cellSize)
            {
                for(int k=0;k<mSnake.x.length;k++)
                {
                    if(i == mSnake.x[k] && j==mSnake.y[k])
                    {
                        Button b = new Button();
                        b.setSize(10,10);
                        b.setBackground(Color.WHITE);
                        b.setLocation(i,j);
                        b.setVisible(true);
                        panel.add(b);
                    }

                }

                if(i== food_x && j== food_y)
                {
                    Button b = new Button();
                    b.setSize(10,10);
                    b.setBackground(Color.RED);
                    b.setLocation(i,j);
                    b.setVisible(true);
                    panel.add(b);
                }
            }
        }

    }

    private void init() {

        //Snake init

        mSnake = new Snake(Direction.right,new int[]{70 ,60 ,50},new int[]{50,50,50},this);
        food_x = (new Random().nextInt(25) * 10)+10;
        food_y = (new Random().nextInt(25) * 10)+10;
        panel = new JPanel();
        panel.setSize(w,h);
        panel.setBackground(Color.BLACK);

        this.add(panel);

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

        int code = e.getKeyCode();
        if(code == KeyEvent.VK_RIGHT)
            mSnake.setDirection(Direction.right);
        else if(code == KeyEvent.VK_LEFT)
            mSnake.setDirection(Direction.left);
        else if(code == KeyEvent.VK_UP)
            mSnake.setDirection(Direction.up);
        else if(code == KeyEvent.VK_DOWN)
            mSnake.setDirection(Direction.down);
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}

class NN implements Serializable {

    private Vector<Integer> tplgy;//neuron numbers in each layer  ,  example : 2 4 1 , 12 16 16 8 ...(inputLayer [ hidden Layers ] outputLayer)
    private java.util.List<double[][]> weights;//weights between each layer
    private java.util.List<double[]> inputs;//input read from data.txt file
    private java.util.List<double[]> targets;//read from data.txt
    private java.util.List<double[]> outputs;//for using while back prop , it keeps the outputs of each neurons in each layer while feedforwarding
    private java.util.List<double[]> gradients;//calculated gradients for each neuron
    public String dataFilePath;
    private int incorrect=0;
    private int correct=0;
    private int total = 0;

    public void init(){
        //initializing weights

        weights = new ArrayList<>(tplgy.size()-1);//layer number -  1

        gradients = new ArrayList<>(tplgy.size()); //same as layer number ,

        outputs = new ArrayList<>(tplgy.size());//same as layer number
        for(int i=0;i<tplgy.size()-1;i++)
        {
            weights.add(new double[tplgy.get(i)+1][tplgy.get(i+1)]);
            weights.set(i,randomize(weights.get(i)));
        }


        //initializing inputs and targets
        inputs = new ArrayList<>(1000);
        targets = new ArrayList<>(1000);
        try {
            Scanner ms = new Scanner(new File(this.dataFilePath));

            int i = 0;
            while(ms.hasNextLine())
            {
                String line = ms.nextLine();
                if(!ms.hasNextLine())
                    break;
                String[] columns = line.split(" ");

                double[] tmp = new double[tplgy.get(0)];

                //System.out.print("inputs : ");
                for (int k=0; k<columns.length; k++) {
                    //System.out.println(columns[i]);
                    Double temp = new Double(Double.parseDouble(columns[k]));
                    tmp[k] = temp;
                    //System.out.print(" "+ temp +" ");
                }
                //System.out.println();

                inputs.add(tmp);


                String line2 = ms.nextLine();
                String[] columns2 = line2.split(" ");

                double[] tmp2 = new double[tplgy.lastElement()];

                //System.out.print("targets : ");
                for (int k=0; k<tplgy.lastElement(); k++) {
                    //System.out.println(columns[i]);
                    Double temp = new Double(Double.parseDouble(columns2[k]));
                    tmp2[k] = temp;
                    //System.out.print(" "+ temp +" ");
                }

                System.out.println(i++);

                targets.add(tmp2);
                i++;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



    }

    ///adding bias neuron to each layer while feedforward
    private double[] addBias(final double[] tmp) {

        double [] rtn = new double[tmp.length+1];
        for(int i=0;i<tmp.length;i++)
            rtn[i] = tmp[i];
        rtn[rtn.length-1] = 1.0;

        return rtn;

    }

    int EPOCH = 2000;
    public void train() {
        assert (inputs.size() == targets.size());

        if (existingNN() == null)
        {
            for (int i = 0; i < EPOCH && netRecentAvrgError > 0.1; i++) {


                for (int j = 0; j < inputs.size(); j++) {
                    double[] result = forwardFeed(inputs.get(j));

                    reportError(result, targets.get(j));
                    backProp(result, targets.get(j));

                    //after a feed forward and a backprop , we have nothing to do with current neuron outputs and gradients
                    //we have to clear them , or else it does not work
                    outputs.clear();
                    gradients.clear();
                }

                System.out.println("net AVG ERROR : " + netRecentAvrgError);


            }

            serializeObject(this);
        }

    }

    public static NN deserializeObject() {
        NN nnDes = null;


        ObjectInputStream in = null;
        FileInputStream file2 = null;
        try {
            file2 = new FileInputStream("./src/NN.ser");
            in = new ObjectInputStream(file2);
            nnDes = (NN) in.readObject();

            in.close();
            file2.close();
        } catch (Exception e) {
            e.printStackTrace();

        }

        return nnDes;
    }

    public static NN existingNN(){
        if(new File("./src/NN.ser").exists())
        {
            NN nnDes = deserializeObject();

            return nnDes;
        }
        else
            return null;
    }

    public void serializeObject(NN object)
    {
        FileOutputStream file = null;
        try {
            file = new FileOutputStream("./src/NN.ser");
            ObjectOutputStream out = new ObjectOutputStream(file);

            // Method for serialization of object
            out.writeObject(object);
            out.close();
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void backProp(final double[] result,final double[] ts) {

        double [] deltas = new double[result.length];
        List<double[]> tmp = new ArrayList<>();
        tmp.add(new double[outputs.get(outputs.size()-1).length]);
        for(int i=0;i<tplgy.lastElement();i++)
        {
            deltas[i] = (result[i]-ts[i])*(outputs.get(outputs.size()-1)[i]*(1-outputs.get(outputs.size()-1)[i]));
            tmp.get(0)[i] = deltas[i];
        }

        int index = 0;
        //let s calculate all the neuron gradients through layers
        //
        for(int i=tplgy.size()-2;i>=0;i--)
        {

            double[] darr = new double[outputs.get(i).length];
            for(int j=0;j<outputs.get(i).length;j++)
            {
                for(int l=0;l<weights.get(i)[0].length;l++)
                {
                    darr[j] += weights.get(i)[j][l] * tmp.get(index)[l];
                }
                darr[j] *= outputs.get(i)[j] * (1-outputs.get(i)[j]);//derivative of sigmoid

            }
            index++;
            tmp.add(darr);
        }

        //
        for(int i=tmp.size()-1;i>=0;i--)
        {
            gradients.add(tmp.get(i));
        }

        // update weights

        for(int i=0;i<weights.size();i++)
        {
            double [] biased = addBias(outputs.get(i));
            for(int j=0;j<weights.get(i).length;j++)
            {
                for(int k=0;k<weights.get(i)[j].length;k++)
                {

                    weights.get(i)[j][k] -= gradients.get(i+1)[k] * biased[j]*0.015;//0.15 is the learning rate
                }
            }
        }




    }

    private double netRecentAvrgError = 1.0;

    private void reportError(final double[] result, final double[] ts) {


        double error = 0.0;

        for(int i=0;i<tplgy.lastElement();i++)
        {
            error += (result[i] - ts[i]) * (result[i] - ts[i]);
        }

        error /= result.length; // get average error squared
        error =Math.sqrt(error); // RMS

        // Implement a recent average measurement

        //the number 1000 is the smoothing factor here
        netRecentAvrgError =(netRecentAvrgError * 1000 + error)/(1000 + 1.0);

        System.out.println("Error ::: "+netRecentAvrgError);


    }

    public Direction feedAndGetDir(final Vector<Double> input)
    {
        double[] inputarr = new double[input.size()];
        for(int i=0;i<input.size();i++)
            inputarr[i] = input.get(i);
        double[] result = forwardFeed(inputarr);

        double max = result[0] ;
        Direction dir = Direction.right;

        if(result[1] > max)
        {dir = Direction.up; max = result[1];};
        if(result[2] > max)
        {dir = Direction.left; max = result[2];}
        if(result[3] > max)
        {
            dir = Direction.down; max = result[3];
        }
        return dir;
    }

    public double[] forwardFeed(final double[] input) {

        double[] result = new double[input.length];
        for(int i=0;i<result.length;i++)
            result[i] = input[i];

        outputs.clear();
        gradients.clear();
        outputs.add(result);
        for(int i=0;i<weights.size();i++)
        {
            outputs.add(i+1,mMultiply(addBias(outputs.get(i)),weights.get(i)));
        }



        return outputs.get(outputs.size()-1);

    }

    private double[] mMultiply(final double [] vector,final double[][] ws){

        double [] rtn = new double[ws[0].length];

        for(int i=0;i<ws[0].length;i++)
        {
            double val = 0.0;
            for(int j=0;j<vector.length;j++)
            {
                val += vector[j] * ws[j][i];
            }

            rtn[i] = transferFunc(val);
        }

        return rtn;
    }

    private double transferFunc(double x) {

        return (1/(1+ Math.pow(Math.E, -x)));
    }


    public double[][] randomize(double [][] arg) {
        Random r = new Random();
        for (int i = 0; i < arg.length; i++)
        {
            for (int j = 0; j < arg[i].length; j++) {
                arg[i][j] = r.nextInt() % 2 == 0 ? r.nextDouble() : -1 * r.nextDouble();

                //arg[i][j] = r.nextDouble();
            }

        }
        return arg;

    }

    public NN(Vector<Integer> topology,String dataPath){
        tplgy = topology;this.dataFilePath = dataPath;
    }

    public void test(String filename)
    {
        Scanner ms = null;
        try {
            ms = new Scanner(new File(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        while(ms.hasNextLine())
        {
            String line = ms.nextLine();
            String[] columns = line.split(" ");

            double[] inpt = new double[tplgy.get(0)];

            //System.out.print("inputs : ");
            for (int k=0; k<columns.length; k++) {
                //System.out.println(columns[i]);
                Double temp = new Double(Double.parseDouble(columns[k]));
                inpt[k] = temp;
                //System.out.print(" "+ temp +" ");
            }
            //System.out.println();

            double [] result = this.forwardFeed(inpt);
            String line2 = ms.nextLine();
            String[] columns2 = line2.split(" ");

            double[] target = new double[tplgy.lastElement()];

            //System.out.print("targets : ");
            for (int k=0; k<tplgy.lastElement(); k++) {
                //System.out.println(columns[i]);
                Double temp = new Double(Double.parseDouble(columns2[k]));
                target[k] = temp;
                //System.out.print(" "+ temp +" ");
            }

            System.out.print("result:");
            for(int i=0;i<target.length;i++)
            {
                System.out.print(" "+result[i]);
            }

            System.out.print("target:");
            for(int i=0;i<target.length;i++)
            {
                System.out.print(" "+target[i]);
            }


            if((target[0] == 1.0 && max(0,result)) || (target[1] == 1.0 && max(1,result)) || (target[2] == 1.0 && max(2,result)) ||
                    (target[3] == 1.0 && max(3,result)) || (target[4] == 1.0 && max(4,result)) || (target[5] == 1.0 && max(5,result))
                    || (target[6] == 1.0 && max(6,result)) || (target[7] == 1.0 && max(7,result)) || (target[8] == 1.0 && max(8,result)) ||
                    (target[9] == 1.0 && max(9,result)))
            {
                correct++;
            }
            else
                incorrect++;

            total++;
            System.out.println("Correct : "+correct);
            System.out.println("inCorrect : "+incorrect);
            System.out.println("Accurecy : "+((double)correct/(double)total));

        }
    }

    private boolean max(int i, double[] result) {

        double max = 0.0;
        for(int k=0;k<result.length;k++)
            if(result[k] > result[i])
                return false;

        return true;
    }
}
