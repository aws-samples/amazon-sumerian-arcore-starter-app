## amazon-sumerian-arcore-starter-app

A sample Android project that demonstrates how to create a simple augmented reality experience using Google's ARCore with the Amazon Sumerian service.

## License

This library is licensed under the Apache 2.0 License.

## Important Note

When using Sumerian's [private publishing option](https://aws-amplify.github.io/docs/js/xr) with AWS Amplify, it's necessary to make the hosting page's background transparent, otherwise the default opaque background will occlude the device's camera image. This can be accomplished by executing the following line of Javascript on the hosting page:

`document.body.style.backgroundColor = 'transparent';`

or by setting the same property on the page's stylesheet.
