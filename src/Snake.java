



public class Snake
{
    public Direction direction;
    public int [] x;
    public int [] y;
    public int length = 3;
    private boolean grow = false;
    Board brd ;

    public Snake(Direction dir, int[] x, int[] y, Board board)
    {
        this.x = x;
        this.y = y;
        direction = dir;
        brd= board;
    }


    public void setDirection(Direction dir)
    {
        this.direction = dir;
    }
    public void grow(){
        this.grow = true;
    }



    public void move(){

        int growx = x[x.length-1];
        int growy = y[y.length-1];



        for(int i=x.length-1;i>0;i--)
        {
            x[i] = x[i-1];
            y[i] = y[i-1];


        }

        if(direction.equals(Direction.right))
        {
            x[0] += 10;
            x[0] = x[0]>brd.getWidth() ? 0 : x[0];
        }
        else if(direction.equals(Direction.up))
        {
            y[0] -= 10;
            y[0] = y[0]<0 ? brd.getHeight()-10 : y[0];

        }
        else if(direction.equals(Direction.left))
        {
            x[0] -= 10;
            x[0] = x[0]<0 ? brd.getWidth()-10 : x[0];
        }
        else
            y[0] +=10;
        y[0] = y[0]>brd.getHeight() ? 0 : y[0];


        if(grow)
        {
            int [] newX = new int[x.length+1];
            int [] newY = new int[y.length+1];

            for(int i=0;i<x.length;i++)
            {
                newX[i] = x[i];
                newY[i] = y[i];
            }
            newX[x.length] = growx;
            newY[y.length] = growy;

            x = newX;
            y = newY;
            this.grow = false;

        }

    }
}