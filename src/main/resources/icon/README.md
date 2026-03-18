# README

To create the Apple icon run the following commands:

```shell
sips -z 16 16     swing-icon-original.png --out swing-icon.iconset/icon_16x16.png
sips -z 32 32     swing-icon-original.png --out swing-icon.iconset/icon_16x16@2x.png
sips -z 32 32     swing-icon-original.png --out swing-icon.iconset/icon_32x32.png
sips -z 64 64     swing-icon-original.png --out swing-icon.iconset/icon_32x32@2x.png
sips -z 128 128   swing-icon-original.png --out swing-icon.iconset/icon_128x128.png
sips -z 256 256   swing-icon-original.png --out swing-icon.iconset/icon_128x128@2x.png
sips -z 256 256   swing-icon-original.png --out swing-icon.iconset/icon_256x256.png
sips -z 512 512   swing-icon-original.png --out swing-icon.iconset/icon_256x256@2x.png
sips -z 512 512   swing-icon-original.png --out swing-icon.iconset/icon_512x512.png
sips -z 1024 1024 swing-icon-original.png --out swing-icon.iconset/icon_512x512@2x.png
```

```shell
iconutil -c icns swing-icon.iconset
```