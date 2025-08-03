本博客包含以下内容

- [x] fragment的生命周期
- [x] 用静态fragment来处理双面版布局
- [ ] 动态fragment
- [ ] jetpack导航

可以看到本博客主要讲的是fragment

## fragment的生命周期

[fragment的生命周期](https://developer.android.com/guide/fragments?hl=zh-cn)与activity的非常相似,事实上他们的回调也是交错进行的.

![image-20250803163733931](../../../home/zimang/.config/Typora/typora-user-images/image-20250803163733931.png)

### override fun onAttach(context: Context)

在这个方法中，你的fragment与它所在的activity建立了联系。这使你能够引用活动，尽管在这个阶段，碎片和活动都还没有完全创建(created)。

**要点**

1. 碎片与活动绑定
2. 碎片与活动都没有完全创建



### override fun onCreate(savedInstanceState: Bundle?)

这是进行任何初始化的地方。在这里不设置片段的布局，因为在这个阶段，没有可用于显示的UI，也没有像在活动中那样的`setContentView`可用。与活动的`onCreate()`函数相同，您可以使用`savedInstanceState`参数在片段重新创建时恢复片段的状态。

**要点**

1. 同活动的onCreate一样进行一切初始化,用`saveInstanceState`恢复碎片的状态
2. 但是因为没有可用于显示的UI这里无法设置碎片的布局.



### override fun onCreateView(inflater: LayoutInflater, container:ViewGroup?, savedInstanceState: Bundle?): View?

这里是你可以创建片段布局的地方。在这里最重要的一点是，与活动设置布局不同，片段实际上会从这个函数返回布局View?。你的布局中的视图可以在这里引用，但有一些注意事项。你需要在引用其中包含的视图之前创建布局，这就是为什么最好在onViewCreated中进行视图操作。

**要点**

1. 创建布局
2. 返回view
3. !! 在引用布局之前创建布局,最好在`onViewCreated`中进行视图操作



### override fun onViewCreated(view View, savedInstanceState: Bundle?)

这个回调函数位于片段完全创建和对用户可见之间。在这里，通常会设置视图并向这些视图添加任何功能和交互性。例如，可以为按钮添加一个OnClickListener，并在点击时调用一个函数。

**要点**

1. 设置视图为其添加功能与交互
2. 处于完全创建与对用户可见之间

### override fun onActivityCreated(context: Context)

这个方法会在活动的onCreate方法执行完毕后立即调用。大部分片段视图状态的初始化工作已经完成，如果需要进行最终设置，这就是执行的地方。

**要点**

1. 做最终的配置
2. 在活动的onCreate方法执行后立即调用

### override fun onStart()

当片段即将对用户可见，但尚未可供用户交互时，将调用此函数。

### override fun onResume()

调用结束时结束时，用户可以与您的片段进行交互。通常，在此回调中没有定义太多的设置或功能，因为当应用程序进入后台然后再次回到前台时，将始终调用此回调。因此，您不希望在片段变得可见时不必要地重复设置片段，而可以通过一个在片段变得可见时不运行的回调来完成。

**要点**

1. 不要在此进行太多设置,这里会在用户使用场景中频繁触发
2. 回调结束后用户即可进行交互

### override fun onPause()

与其对应的是，活动中的onPause()表示您的应用程序即将进入后台或者被屏幕上的其他内容部分覆盖。请使用此方法保存片段状态的任何更改。

**要点**

1. 在此保存片段的状态
2. 应用即将进入后台或者被其他内容覆盖时调用

### override fun onStop()

回调结束后,片段不再可见



### override fun onDestroyView()

通常在片段被销毁之前会调用这个方法进行最后的清理工作。如果需要清理任何资源，应该使用这个回调函数。如果片段被推到后退栈并保留，那么即使不销毁片段也会调用这个方法。在完成这个回调后，片段的布局视图会被移除。

**要点**

1. 在这个回调进行资源回收
2. 碎片清除或者碎片没有被销毁而是推到back stack并保留也会调用这个方法



### override fun onDestroy()

该片段正在被销毁。这可能是因为应用程序被终止，也可能是因为这个片段正在被另一个片段替换。

**要点**

1. 正在被销毁时调用
2. 正在被其他碎片替换时调用

### override fun onDetach()

当片段从其活动中分离时会调用此方法。



### 常用回调

还有更多的片段回调，但这些是您在大多数情况下会使用的。

通常，您只会使用这些回调的一个子集：

1. onAttach()用于将活动与片段关联
2. onCreate用于初始化片段
3. onCreateView用于设置布局
4. 在`onViewCreated`/`onActivityCreated`中进行进一步的初始化
5. 也许在`onPause()`中进行一些清理工作。



### 碎片与活动生命周期交错

当前详细介绍的活动和片段生命周期之间的交互是针对静态片段的情况，即在活动的布局中定义的片段被创建时。对于动态片段，也就是在活动已经运行时添加的片段，交互可能会有所不同。

这里没有打印碎片 `onResume` ,`onStart`的日志,应该是不太常用

```bash
##启动
2025-08-03 20:36:02.652  9209-9209  MainFragment            com.example.fragmentlifecycle        D  onAttach
2025-08-03 20:36:02.653  9209-9209  MainFragment            com.example.fragmentlifecycle        D  onCreate
2025-08-03 20:36:02.654  9209-9209  MainFragment            com.example.fragmentlifecycle        D  onCreateView
2025-08-03 20:36:02.662  9209-9209  MainFragment            com.example.fragmentlifecycle        D  onViewCreated
2025-08-03 20:36:02.664  9209-9209  MainActivity            com.example.fragmentlifecycle        D  onCreate: 
2025-08-03 20:36:02.683  9209-9209  MainActivity            com.example.fragmentlifecycle        D  onStart: 
2025-08-03 20:36:02.685  9209-9209  MainActivity            com.example.fragmentlifecycle        D  onResume: 

##返回至桌面
2025-08-03 20:37:49.742  9209-9209  MainFragment            com.example.fragmentlifecycle        D  onPause
2025-08-03 20:37:49.750  9209-9209  MainActivity            com.example.fragmentlifecycle        D  onPause
2025-08-03 20:37:49.775  9209-9209  MainFragment            com.example.fragmentlifecycle        D  onStop
2025-08-03 20:37:49.777  9209-9209  MainActivity            com.example.fragmentlifecycle        D  onStop


##运行状态关闭
2025-08-03 20:46:04.843  9680-9680  MainFragment            com.example.fragmentlifecycle        D  onPause
2025-08-03 20:46:04.844  9680-9680  MainActivity            com.example.fragmentlifecycle        D  onPause
2025-08-03 20:46:04.877  9680-9680  MainFragment            com.example.fragmentlifecycle        D  onStop
2025-08-03 20:46:04.877  9680-9680  MainActivity            com.example.fragmentlifecycle        D  onStop
2025-08-03 20:46:04.888  9680-9680  MainFragment            com.example.fragmentlifecycle        D  onDestroyView
2025-08-03 20:46:04.893  9680-9680  MainFragment            com.example.fragmentlifecycle        D  onDestroy
2025-08-03 20:46:04.893  9680-9680  MainFragment            com.example.fragmentlifecycle        D  onDetach
2025-08-03 20:46:04.895  9680-9680  MainActivity            com.example.fragmentlifecycle        D  onDestroy
```

对于大多数情况，您只会使用前面的片段回调。  

**要点**

1. 碎片在活动之前创建,在活动之前销毁
2. 可以将功能封装到碎片中以一个活动多个碎片的形式开发,而不用将所有功能放到活动里面. 



## 用静态fragment来处理双面版布局

Android允许根据许多不同的形态因素指定不同的资源。在res（资源）文件夹中经常用于定义平板的限定符是sw600dp。这表示如果设备的最短宽度（sw）超过600 dp，则使用这些资源。这个限定符用于7英寸平板及更大的设备。平板设备支持所谓的双窗格布局。一个窗格代表用户界面的一个独立部分。如果屏幕足够大，那么可以支持两个窗格（双窗格布局）。这也提供了一个窗格与另一个窗格互动以更新内容的机会。

为不同的屏幕尺寸创建不同的布局和资源。这用于根据设备是手机还是平板来决定显示哪些资源。



1. 不同尺寸
   1. 创建不同条件下的layout
   2. 根据是否能找到不同layout下的id来判断当前处于那个layout
2. 双面布局 (一个活动控制两个碎片)
   1. 上下文暴露接口控制fragment内容展示
   2. fragment保存上下文
   3. 调用上下文提供的方法更新其他fragment的内容
3. 单面布局 (两个活动分别控制两个碎片)
   1. 用intent来跳转活动

**接口**

1. `FragmentManager`和`SupportFragmentManager`
   1. 前者是api 11之后用于管理活动与碎片交互的类
   2. 后者是用于支持api 11之前的库. 被作为jetpack fragment库的基石更进一步开发,为管理fragment增加了一些优化
2. `SupportFragmentManager`提供的`findFragmentById`和`findFragmentByTag`
   1. 在`<fragment xxx`中添加`android:tag="myTag"`即可用于byTag引索
   2. 用`<fragmen xxx`的id做引索



## 动态fragment

到目前为止，你只看到了在编译时以 XML 格式添加片段。虽然这可以满足许多使用情况，但你可能希望在运行时动态添加片段以响应用户的操作。**这可以通过将 ViewGroup 作为片段的容器，然后向 ViewGroup 添加、替换和移除片段来实现。**

**您需要在 app/build.gradle 文件中的 dependences{ } 块中添加 `FragmentContainerView`的依赖`androidx.fragment:fragment-ktx` . `FragmentContainerView`是一个优化的 ViewGroup，用于处理片段事务。**

这种技术更加灵活，因为片段可以在不再需要时保持活动状态，然后被移除，而不像静态片段那样总是在 XML 布局中被膨胀。**如果在一个活动中需要三到四个以上的片段来满足不同的用户路径，那么首选的选项是通过对用户界面的交互动态地添加/替换片段。**当用户与用户界面的交互在编译时是固定的，并且您事先知道需要多少个片段时，使用静态片段效果更好。例如，从列表中选择项目以显示内容就是这种情况。