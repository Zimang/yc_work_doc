## padding和margin的区别

填充(padding)应用在视图的内部，是文本和边框之间的空间。边距(margin)则是指定在视图的外部，是视图外边缘的空间。

例如，android:padding 设置视图的填充，具有指定值的所有边。或者，您可以使用 android:paddingTop、android:paddingBottom、android:paddingStart 和 android:paddingEnd 之一来指定视图的四个边之一的填充。

这种模式也适用于指定边距，因此 android:layout_margin 指定视图四个边的边距值，而 android:layout_marginTop、android:layout_marginBottom、android:layout_marginStart 和 android:layout_marginEnd 允许设置各个边的边距。

## tools:text=""和android:text=""的区别

设计时与运行时的关键区别

1. android:text: 此属性设置应用在设备或模拟器上运行时将显示的实际文本。它被打包到您的 APK 文件中。
2. tools:text: 此属性仅用于 Android Studio 布局编辑器中的设计时预览。它允许您查看布局如何显示一些示例数据，而无需将这些数据硬编码到您的应用中。当构建应用时，构建工具会移除所有工具属性。这意味着除非您也有 android:text 属性或以编程方式设置文本，否则“XHFG6H9O”不会出现在运行的应用程序中。

为什么 tools:text 很有用？

1. 预览动态数据：如果您的 TextView 将在运行时由网络请求、数据库或用户输入填充数据，您可以使用 tools:text 查看布局如何适应特定长度或格式的文本。
2. 避免硬编码的预览数据：您无需为预览目的临时将真实数据放入 android:text 中，然后记得将其删除。
3. 使用数据绑定：通常使用 tools:text 预览数据绑定文本的外观。
4. 查看占位内容：它有助于可视化将动态填充的列表项或其他 UI 元素。

## 复用xml layout文件

```xml
<include layout="@layout/numbers"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintTop_toBottomOf="@id/number"/>
```