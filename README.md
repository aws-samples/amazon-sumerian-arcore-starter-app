## 亚马逊苏美尔人的arcore入门应用程序

一个示例Android项目，演示了如何使用Google的ARCore和Amazon Sumerian服务创建简单的增强现实体验。

## License

该库已根据Apache 2.0许可获得许可。

## Important Note

当将Sumerian的[私有发布选项]（https://aws-amplify.github.io/docs/js/xr）与AWS Amplify结合使用时，有必要使托管页面的背景透明，否则默认的不透明背景将遮挡设备的背景。相机图像。这可以通过在托管页面上执行以下Javascript行来实现：

`document.body.style.backgroundColor = 'transparent';`

或通过在页面样式表上设置相同的属性。
