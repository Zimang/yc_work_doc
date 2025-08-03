这一片博客包括以下内容

- [x] activity的生命周期
- [x] 保存与还原activity的状态
- [x] activity同intent的交互
- [x] intent,tasks,以及启动模式



## activity的生命周期

您的应用程序创建,展示,隐藏、后台运行，然后销毁时会有相应的步骤。这个过程被称为Activity生命周期。在每一个步骤中，都有一个回调函数可供您的Activity使用，以执行诸如创建和更改显示以及在应用程序被放入后台时保存数据，然后在应用程序回到前台后恢复数据等操作。

 ![image-20250802232415727](../../../home/zimang/.config/Typora/typora-user-images/image-20250802232415727.png)

1. 图片有些问题,`onResume`结束之后过一小会activity才可见



### override fun onCreate(savedInstanceState: Bundle?)

这是你在进行绘制全屏幕活动时会使用最多的回调函数。在这里，你会准备好要显示的活动布局。

通常你会通过调用`setContentView(R.layout.activity_main)`方法来设置活动的用户界面，并进行任何必要的初始化工作。

该方法在其生命周期中只会被调用一次，除非`Activity`被再次创建。这种情况默认发生在某些操作中（例如将手机从纵向旋转为横向）。`Bundle?`类型的`savedInstanceState`参数（`?`表示该类型可以为空）在其最简单的形式中是一组键值对的映射，经过优化以保存和恢复数据。如果这是应用程序启动后首次运行Activity，或者Activity首次创建，或者Activity在没有保存任何状态的情况下被重新创建，`savedInstanceState`将为空。

**要点**

1. 在此调用`setContentView(R.layout.activity_main)`设置UI界面
2. 用`Bundle?`类型的`savedInstanceState`参数恢复数据

### override fun onRestart()

当活动重新启动时，这会在`onStart()`之前立即调用。清楚地了解重新启动活动和重新创建活动之间的区别很重要。当用户按下主页按钮将活动置于后台时，当活动再次回到前台时，将调用`onRestart()`。重新创建活动是指发生配置更改时，比如设备旋转。活动会被销毁然后重新创建，在这种情况下，不会调用onRestart()。

**要点**

1. 理解重新启动activity和重新创建activity的区别
2. `onRestart`的启动时机是在重新启动activity的时候,通常是从后台转至前台

### override fun onStart()

这是当活动从后台切换到前台时发出的第一个回调。

### override fun onRestoreInstanceState(savedInstanceState: Bundle?)

如果状态已经通过`onSaveInstanceState(outState: Bundle?)`保存，这个方法会在`onStart()`之后被系统调用，你可以在这里检索`Bundle`状态，而不是在`onCreate(savedInstanceState: Bundle?)`中恢复状态。

**要点**

1. 和`onSaveInstanceState`保存并还原状态
2. 已知恢复状态的两种方法,一个是`onCreate`的时候和`onStart`之后恢复状态



### override fun onResume()

这个回调函数在首次创建活动的最后阶段运行，也会在应用程序被置于后台然后重新进入前台时运行。在这个回调函数完成后，屏幕/活动已准备就绪，可以接收用户事件并做出响应。

**要点**

1. 这个回调结束后代表着activity可以进行用户交互
2. 在首次创建活动的最后阶段与后台转向前台两个时间出现



### override fun onSaveInstanceState(outState: Bundle?)

如果你想保存活动的状态，这个函数可以做到。你可以使用其中一个便利函数添加键值对，具体取决于数据类型。这样，当你的活动在`onCreate(saveInstanceState: Bundle?)`和`onRestoreInstanceState(savedInstanceState: Bundle?)`中重新创建时，数据将会保留。

1. 该函数通过`outState`保存数据给onCreate和onRestoreInstanceState两个回调调用



### override fun onPause()

当Activity开始被置于后台，或者另一个对话框或Activity进入前台时，会调用此函数。

### override fun onStop()

当Activity被隐藏时会调用此函数，可能是因为它被置于后台，或者另一个Activity被启动覆盖在其上。

### override fun onDestroy()

系统会在系统资源不足时、在Activity上显式调用`finish()`方法时，或者更常见的情况是用户通过最近任务/概览按钮关闭应用时，调用此函数来终止Activity。

**要点**

- 调用时间
  1. 系统调用
  2. finish()
  3. 用户手动



## 保存与还原activity的状态

我们主要以一个例子来介绍:屏幕旋转

1. 对于EditText字段，如果它们有设置ID，Android框架会保留字段的状态。
2. 对于其他字段类型，比如TextView，设置ID并不能保留状态，如果您更新它们，就需要自己保存状态。对于支持滚动的视图，比如RecyclerView，设置ID也很重要，因为这样可以在Activity重新创建时保持滚动位置。
3. 这个时候即可通过`onSaveInstanceState(outState: Bundle)`和`onCreate(savedInstanceState: Bundle?)/onRestoreInstanceState(savedInstanceState: Bundle)`函数来保存和恢复实例状态
4. Android框架还提供了ViewModel，这是一个具有生命周期意识的Android架构组件。如何保存和恢复这个状态（使用ViewModel）的机制是由框架管理的，因此你不必像在前面的例子中那样显式管理它。

```kotlin
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d(TAG, "onRestoreInstanceState")
        // Get the discount code or an empty string if it hasn't been set
        discountCode.text = savedInstanceState.getString(DISCOUNT_CODE,"")
        // Get the discount confirmation message or an empty string if it hasn't been set
        discountCodeConfirmation.text = savedInstanceState.getString(DISCOUNT_CONFIRMATION_MESSAGE,"")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState")
        outState.putString(DISCOUNT_CODE, discountCode.text.toString())
        outState.putString(DISCOUNT_CONFIRMATION_MESSAGE, discountCodeConfirmation.text.toString())
    }

```

**要点**

1. 有id的editText会保存状态,其他的如TextView就不会.
2. 对于recycleView的id有助于保存滑动位置的状态
3. 使用onSaveInstanceState,onRestoreInstanceState和viewmodel的两种方法来保持状态





## activity同intent的交互

在Android中，Intent是组件之间的通信机制。

在您自己的应用程序中，很多时候，当当前活动发生某些操作时，您可能希望另一个特定的活动开始。明确指定将启动哪个活动称为显式Intent。

在其他情况下，您可能希望访问系统组件，比如相机。由于您无法直接访问这些组件，您将不得不发送一个Intent，系统会解析以打开相机。这被称为隐式Intent。

为了注册响应这些事件，必须设置Intent过滤器。打开AndroidManifest.xml文件，您将看到在`MainActivity`的`<intent-filter>` XML元素中设置了两个Intent过滤器的示例。

指定为`<action android:name="android.intent.action.MAIN" />`的意思是这是应用程序的主要入口点。根据设置的类别，它决定了应用程序启动时首先启动的Activity。

另一个指定的意图过滤器是`<category android:name="android.intent.category.LAUNCHER" />`，它定义了应用程序应该出现在启动器中。

当结合在一起时，这两个意图过滤器定义了从启动器启动应用程序时应该启动MainActivity。

移除`<action android:name="android.intent.action.MAIN" />`意图过滤器会导致显示`"Error running 'app': Default Activity not found"`的消息。因为应用程序没有主要入口点，所以无法启动。

如果移除`<category android:name="android.intent.category.LAUNCHER" />`，那么就没有地方可以从中启动它。

users flow 

1. `startActivity(intent)`  单向,跳转到另一个activity
2. `registerForActivityResult`创建`ActivityResultLauncher.launch`, `setResult`+`finish` 单向返回,返回结果用`intent`封装,`ActivtiyResultLauncher`写好回调

**要点**

1. 两个常见过滤器,启动入口点与`Launcher`可见
2. 两个acitivity数据流:单向与单向返回



## intent,tasks,以及启动模式

[启动模式](https://developer.android.com/guide/topics/manifest/activity-element#lmode)

1. standard: AAAA  可能会反复创建栈顶的同一个activity
2. singleTop: 1  复用栈顶activity, 回调为`onNewIntent`

登陆验证场景的三种实现

1. singleTop: 在onNewIntent中处理验证逻辑
2. standard: 将展示与验证分离到子activity,添加返回按钮
3. registerActivityForResult: 将验证分离到子activity,返回后展示结果
