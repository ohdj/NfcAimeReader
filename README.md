# NFC Aime Reader

一个尝试通过安卓手机 NFC 读取 Aime 为 SEGA / KONAMI 系游戏刷卡的项目

~~不过没做完，所以是尝试~~

## :thinking: Q&A

**1. 读出来的卡号和卡片背面的不一样**

因为是用读取到的 IDm 补齐位数后作为卡号使用的

**2. 为什么使用 IDm 作为卡号，而不是卡片背面的 ACCESS CODE**

[aqua 读取 felica 卡片部分代码实现](https://dev.s-ul.net/NeumPhis/aqua/-/blob/master/src/main/java/icu/samnyan/aqua/sega/aimedb/handler/impl/FeliCaLookupHandler.java#L44)，直接将 IDm 的十进制形式作为游戏内 ACCESS CODE 使用

```java
StringBuilder accessCode = new StringBuilder(
  String.valueOf(((ByteBuf) requestMap.get("idm")).getLong(0)).replaceAll("-","")
);
while (accessCode.length() < 20) {
  accessCode.insert(0, "0");
}
```

- felica.txt 是 16 位 16 进制字符（IDm）

- aime.txt 是 20 位 10 进制数字

- 如果（10 进制 → 16 进制）转换的结果位数不够，则在最前面补 0

felica.txt (IDm) 和 ACCESS CODE 的转换是 Allnet 服务器实现的，而官机返回卡背面 access code 大概率是 sega 从 AIC 的发卡记录查表得出，私服是没有能力获取卡片 ACCESS CODE 的

所以如果你扫描了一张较新的基于 AIC 的 Aime（Felica 类型），它的 Felica IDm 将被提供给游戏。游戏内无法看到正确的"ACCESS CODE"，但 IDm 是每张卡独有的，这样仍然可以追踪你的游戏记录

参考链接：[关于Aime与felica卡号转换的问题 - BEMANICN](https://bemani.cc/d/107-aimefelica/2)

**3. 开这个项目的原因**

自制读卡器仅算物料成本也挺容易达到 `30RMB` 的，但是一张蓝白卡也就是这个价

有张买来收藏的 Aime，能找方法读个卡就行，拿来刷卡我还能经常拿起来看一看

不过如果你需要经常刷卡，玩得很勤，买个读卡器长期插电脑上还是有实际意义的

## :heart: 鸣谢

部分代码参考自 GPT-4 和 [Mageki](https://github.com/Sanheiii/Mageki)
