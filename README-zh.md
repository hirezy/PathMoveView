# PathMoveView

## PathMoveView 主要是通过Paint获取字体的Path，提供给PathMeasure采样。来进一步掌握path 的采样

## 显示效果
<video src="https://github.com/hirezy/PathMoveView/blob/main/blob/Screen_recording_20240528_174523.webm" controls="controls" width="500" height="300">您的浏览器不支持播放该视频！</video>

# 原理

1、通过PathMeasure测量出position和正切的斜率，注意tan和position都是数组，[0]为x或者x方向，[1]为y或者为y方向，当然tan是带方向的矢量，计算公式是 A = ( x1-x2,y1-y2），这些是PathMeasure计算好的。
```java
PathMeasure.getPosTan(mPathMeasure.getLength() * fraction, position, tan);
```
2、另一个重点是反正切，在java三角函数中，arctan是无法确定角度所在象限的，但是atan2时可以的，因为其内部通过判断x，y、atan三者的值，计算出合适的象限分布，从而推到出正确的角度，因此，我们使用atan2可以很容易判断出真正的夹角。

# 注意点
1、Path长度问题: 这里要注意的是，如果采样的是多个文字的Path，那么PathMeasure#getLength值默认是第一个文字的Path，而不是所有文字的总长。

2、Path 切换: 我们拿到的Path是第一个文字的长度，那么如何切到下一个，这里就得使用pathMeasure#nextContour方法

3、采样:采样时需要注意的是，我们要将所有点拿到，之前博客通过PathMeasure#getSegment对片段采样，但不知道为什么有这种方式很多BUG，会出现不稳定的效果。

> 下面方式不适用，会出现过程不稳定、不连续的问题
```java
mPathMeasure.getSegment(0, mPathMeasure.getLength() * value, mAnimPath, true);
```
> 建议采用提前采样，将所有的点保存下来，这样我们自己来控制点的展示，从而避免了不稳定不连续的问题。
```java
public static List<PointF> textPathToPoints(String text, TextPaint paint) {
    Path fontPath = new Path();
    paint.getTextPath(text, 0, text.length(), 0f, paint.getFontSpacing(), fontPath);
    fontPath.close();
    PathMeasure pathMeasure = new PathMeasure(fontPath, false);
    List<PointF> points = new ArrayList<>();
    float[] pos = new float[2];
    do {
        float distance = 0f;
        while (distance < pathMeasure.getLength()) {
            distance += 5f;
            pathMeasure.getPosTan(distance, pos, null);
            points.add(new PointF(pos[0], pos[1]));
        }
    } while (pathMeasure.nextContour());
    return points;
}
```

# 核心绘制代码

```java
int height = getHeight();
int width = getWidth();

float halfOfTextWidth = measureTextWidth / 2f; //计算中心点一半的长度

float baseline = getTextPaintBaseline(mTextPaint); //计算BaseLine
int count = canvas.save();
canvas.translate(width / 2f, height / 2f);  //平移到View中心点


float spanSize = measureTextWidth / textPoints.size();
int finishCount = 0;  //统计完成绘制的文字总数

for (Map.Entry<String,FontText> entry : textPoints.entrySet()){

FontText textPoint = entry.getValue();
int size = textPoint.currentSize;
int pointSize = textPoint.pointFS.size();
float offset = textPoint.index * spanSize; //文字X轴方向的偏移

    mPaint.setColor(textPoint.color);
    for (int i = 0; i < size; i++) {
PointF pointF = textPoint.pointFS.get(i);
//绘制点
        canvas.drawPoint(pointF.x - halfOfTextWidth + offset, pointF.y + baseline, mPaint);
    }
textPoint.currentSize = Math.min(++size,pointSize);
    if(textPoint.currentSize == pointSize){
finishCount++; // 当前绘制到的位置和pointSize
        }
        }
        canvas.restoreToCount(count);

if(finishCount == textPoints.size()){
        //所有的文字都完成绘制的，过1s之后重新绘制
        for (Map.Entry<String,FontText> entry : textPoints.entrySet()){
        entry.getValue().currentSize = 0;
        }
postInvalidateDelayed(1000);
}else {
postInvalidateDelayed(16);
}
```
# 色彩亮度
> RGB颜色模型有个缺点，不能直观调整亮度和色彩饱和度，为了解决这个问题，我们需要引入HSL、HSV、YUV等，这里我们使用HSL。
* 色相（H） 是色彩的基本属性，就是平常所说的颜色名称，如红色、黄色等。
* 饱和度（S） 是指色彩的纯度，越高色彩越纯，低则逐渐变灰，取 0-1f 的数值。
* 亮度（L） ，取 0-1f，增加亮度，颜色会向白色变化；减少亮度，颜色会向黑色变化。

<img src="https://github.com/hirezy/PathMoveView/blob/main/blob/hls.jpg" controls="controls" width="500" height="300"></img>
