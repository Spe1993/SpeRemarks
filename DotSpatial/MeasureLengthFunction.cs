using DotSpatial.Controls;
using DotSpatial.Symbology;
using GeoAPI.Geometries;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace Space.Spatial.DS.CustomFunctions
{

    /// <summary>
    /// 长度测量继承DotSpatial MapFunction （自带的FunctionMode.Pan 这几个都是继承于MapFunction的类)
    /// </summary>
    public class MeasureLengthFunction : MapFunction
    {

        #region 字段

        private const double RadiusOfEarth = 111319.5;
 

        /// <summary>
        /// 折线段的集合
        /// </summary>
        private List<Coordinate> coordinates;

        /// <summary>
        /// 当前测量任务的每段距离
        /// </summary>
        private List<double> curDis;

        /// <summary>
        /// 当前的总长度
        /// </summary>
        private double currentDistance;

        /// <summary>
        /// 加上移动线段的总长度
        /// </summary>
        private double tempDistance;
 
        /// <summary>
        /// 鼠标位置
        /// </summary>
        private Point _mousePosition;

      

        /// <summary>
        /// 这次绘制之前折线段点集合
        /// </summary>
        private List<List<Coordinate>> previousParts;

        /// <summary>
        /// 用于判断是否注销测量
        /// </summary>
        private bool _standBy;

       

        #endregion

        /// <summary>
        /// 构造函数
        /// </summary>
        /// <param name="map"></param>
        public MeasureLengthFunction(IMap map)
            : base(map)
        {


            previousParts = new List<List<Coordinate>>();

            YieldStyle = YieldStyles.LeftButton | YieldStyles.RightButton;


            if (map != null)
            {

                (map as Control).MouseLeave += MapMouseLeave;

            }

            Name = "距离测量";

            coordinates = new List<Coordinate>();

            curDis = new List<double>();
        }


        /// <summary>
        /// 激活测量
        /// </summary>
        protected override void OnActivate()
        {
             
            if (_standBy == false)
            {
                previousParts = new List<List<Coordinate>>();

                coordinates = new List<Coordinate>();

                curDis = new List<double>();
            }

            _standBy = false;
            base.OnActivate();
        }

        /// <summary>
        /// 注销
        /// </summary>
        protected override void OnDeactivate()
        {
            if (_standBy)
            {
                return;
            }

            // Don't completely deactivate, but rather go into standby mode
            // where we draw only the content that we have actually locked in.

            //使用下面两行代码时，绘制注销时将会在地图上清除测量路径
            //previousParts = new List<List<Coordinate>>();
            //coordinates = new List<Coordinate>();

            _standBy = true;

            Map.Invalidate();
        }

        /// <summary>
        /// 鼠标离开控件区域
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void MapMouseLeave(object sender, EventArgs e)
        {
            Map.Invalidate();
        }


        /// <summary>
        /// 地图绘制
        /// </summary>
        /// <param name="e"> </param>
        protected override void OnDraw(MapDrawArgs e)
        {
            //当前鼠标在屏幕坐标系下的坐标
            Point mouseTest = Map.PointToClient(Control.MousePosition);

            //鼠标是否在地图内
            bool hasMouse = Map.ClientRectangle.Contains(mouseTest);

            //画笔 ---蓝色
            Pen bluePen = new Pen(Color.Blue, 2F);

            //画笔---红色
            Pen redPen = new Pen(Color.Red, 3F);

            //红色刷子
            Brush redBrush = new SolidBrush(Color.Red);

            //点序列
            List<Point> points = new List<Point>();

            //字体
            Font drawFont = new Font("Arial", 12);

            //抗锯齿
            e.Graphics.SmoothingMode = SmoothingMode.AntiAlias;


            Brush blue = new SolidBrush(Color.FromArgb(60, 0, 0, 255));

            //绘制历史测量路劲
            if (previousParts != null && previousParts.Count > 0)
            {
                //绘制路劲
                GraphicsPath previous = new GraphicsPath
                {
                    FillMode = FillMode.Winding
                };

                //点序列
                List<Point> allPoints = new List<Point>();

                //获取一次历史记录
                foreach (List<Coordinate> part in previousParts)
                {
                    //获取此次历史记录中的每个点
                    List<Point> prt = new List<Point>();

                    foreach (Coordinate c in part)
                    {
                        prt.Add(Map.ProjToPixel(c));
                    }

                    //将点添加到路劲中绘制出来
                    previous.AddLines(prt.ToArray());

                    //将此次历史记录中的所有点添加到序列中
                    allPoints.AddRange(prt);
                  
                    //结束此次历史路劲的绘制，为下次绘制做准备
                    previous.StartFigure();
                }

         
                //当所有点大于一的时候
                if (allPoints.Count > 1)
                {
                    //绘制所有历史路劲
                    e.Graphics.DrawPath(bluePen, previous);
                    
                }

                //将所有节点绘制出来（红色）
                foreach (Point pt in allPoints)
                {
                    e.Graphics.FillRectangle(redBrush, new Rectangle(pt.X - 2, pt.Y - 2, 4, 4));
                }
            }


            //绘制当前测量
            if (coordinates != null)
            {
                //获取此次测量的所有点
                foreach (Coordinate coord in coordinates)
                {
                    points.Add(Map.ProjToPixel(coord));
                }

                
                //点数量大于1
                if (points.Count > 1)
                { 
                    //绘制折线，节点，长度
                   
                    for (int i = 0; i < points.Count-1; i++)
                    {
                        Point cp = points[i];

                        Point np = points[i + 1];

                        e.Graphics.DrawLine(bluePen, cp, np);

                        e.Graphics.FillRectangle(redBrush, new Rectangle(cp.X - 2, cp.Y - 2, 4, 4));
                         
                        e.Graphics.DrawString(curDis[i].ToString(), drawFont, redBrush, new Point((cp.X+np.X)/2,(cp.Y+np.Y)/2));
                    }
                }

                //当点数量大于0时，绘制鼠标移动所构成的那条线
                if (points.Count > 0 && _standBy == false && hasMouse)
                {

                    e.Graphics.DrawLine(redPen, points[points.Count - 1], _mousePosition);

                    double dis = GetDist(Map.PixelToProj(_mousePosition), null);

                    e.Graphics.DrawString(dis.ToString(), drawFont, redBrush, new Point((points[points.Count-1].X+_mousePosition.X)/2,(points[points.Count-1].Y+_mousePosition.Y)/2));

                    e.Graphics.DrawString(String.Format("总长度:{0}",tempDistance.ToString()), drawFont, redBrush, _mousePosition);
                    
                }

                 
            }

            //释放
            bluePen.Dispose();
            redPen.Dispose();
            redBrush.Dispose();
            blue.Dispose();
            drawFont.Dispose();
            base.OnDraw(e);
        }

        /// <summary>
        ///  鼠标移动
        /// </summary>
        /// <param name="e"></param>
        protected override void OnMouseMove(GeoMouseArgs e)
        {
            //注销后直接跳过
            if (_standBy)
            {
                return;
            }

            //当无坐标时
            if (coordinates == null || coordinates.Count == 0)
            {
                return;
            }
            //鼠标位置点
            Coordinate c1 = e.GeographicLocation;

            //鼠标和最后一个点的距离
            double dist = GetDist(c1,null);

            tempDistance = currentDistance + dist;
             
            //当点数量大于0时
            if (coordinates.Count > 0)
            {
                //将地理坐标转为屏幕坐标
                List<Point> points = coordinates.Select(coord => Map.ProjToPixel(coord)).ToList();

                //获取鼠标上一个位置和最后一个点的矩形区域
                Rectangle oldRect = SymbologyGlobal.GetRectangle(_mousePosition, points[points.Count - 1]);
                
                //获取鼠标和左后一个点的矩形区域
                Rectangle newRect = SymbologyGlobal.GetRectangle(e.Location, points[points.Count - 1]);

                //合并区域
                Rectangle invalid = Rectangle.Union(newRect, oldRect);

                //刷新区域
                invalid.Inflate(220, 20);

                Map.Invalidate(invalid);
            }

            //设置为鼠标位置
            _mousePosition = e.Location;

            base.OnMouseMove(e);
        }

        /// <summary>
        /// 鼠标弹起
        /// </summary>
        /// <param name="e"> </param>
        protected override void OnMouseUp(GeoMouseArgs e)
        {
            if (_standBy)
            {
                return;
            }


            //右键结束此次测量，开始下次测量
            if (e.Button == MouseButtons.Right)
            {
                if (coordinates.Count > 1)
                {
                    previousParts.Add(coordinates);

                    

                    currentDistance = 0;
                    
                }

                coordinates = new List<Coordinate>();

                curDis = new List<double>();

                Map.Invalidate();
            }
            else
            {

                if (coordinates.Count > 0)
                {
                    //当前点击点
                    Coordinate c1 = e.GeographicLocation;

                    //获取和上一个点的距离
                    double dist = GetDist(c1,null);

                    curDis.Add(dist);

                    //长度叠加
                    currentDistance += dist;

                }

                //添加到序列存储
                coordinates.Add(e.GeographicLocation);
                 
                Map.Invalidate();
            }

            base.OnMouseUp(e);
        }

        /// <summary>
        /// Occurs when this function is removed.
        /// </summary>
        protected override void OnUnload()
        {
            if (Enabled)
            {
                coordinates = null;

                previousParts = null;

            }

            Map.Invalidate();
        }

        /// <summary>
        /// 获取传入点和最后一次记录点的距离
        /// </summary>
        /// <param name="c1"></param>
        /// <returns></returns>
        private double GetDist(Coordinate c1,Coordinate c2)
        {
            if (c2==null)
            {
                c2 = coordinates[coordinates.Count - 1];
            }
            //Coordinate 
            double dx = Math.Abs(c2.X - c1.X);
            double dy = Math.Abs(c2.Y - c1.Y);
            double dist;
            if (Map.Projection != null)
            {
                if (Map.Projection.IsLatLon)
                {
                    double y = (c2.Y + c1.Y) / 2;
                    double factor = Math.Cos(y * Math.PI / 180);
                    dx *= factor;
                    dist = Math.Sqrt((dx * dx) + (dy * dy));
                    dist = dist * RadiusOfEarth;
                }
                else
                {
                    dist = Math.Sqrt((dx * dx) + (dy * dy));
                    dist *= Map.Projection.Unit.Meters;
                }
            }
            else
            {
                dist = Math.Sqrt((dx * dx) + (dy * dy));
            }

            // _measureDialog.Distance = dist;
            return Math.Round(dist,2);
        }

    }
}
