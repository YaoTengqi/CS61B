# Gitlet
本项目是编写一个简易版git

## Classes and Data Structures
### Main
Main文件是整个项目的入口，他根据输入的args[]参数识别命令并执行。

### Blobs

每个blob代表一个文件，保存着文件的具体内容，也是gitlet处理的元数据。

#### 属性

| 属性名  |  格式  |                    描述                    |
| :-----: | :----: | :----------------------------------------: |
| blob_ID | String |      每个blob都有一个由SHA-1生成的ID       |
| content | byte[] | 每个blob将存储文件的内容，压缩为byte[]格式 |

#### 函数

|                        函数名                        | 返回值 |                     描述                     |
| :--------------------------------------------------: | :----: | :------------------------------------------: |
|            public Blobs(String fileName)             |   /    |         根据给定的文件名生成blob对象         |
| public static byte[] readFileToBytes(File blob_file) | byte[] | 给定文件对象，读取文件中的内容到byte[]后返回 |
|   public static String calculateID(byte[] content)   | String |     根据文件数据计算出相应的SHA-1哈希ID      |



### Commit

每个commit代表一次提交，有一个独一无二的ID，提交信息，提交时间，对blobs的引用以及父亲commit

#### 属性

|  属性名   |  格式   |                  描述                   |
| :-------: | :-----: | :-------------------------------------: |
| commit_ID | String  |    每个commit都有一个由SHA-1生成的ID    |
|  message  | String  | 每次提交都会有一个message来描述本次提交 |
|   time    |  Date   |               提交的时间                |
| blobArray | Blobs[] |  本次提交所包含的blob，存储在此队列中   |
|  parent   | commit  |          本次提交的父亲commit           |

#### 函数

|                            函数名                            | 返回值 |                        描述                         |
| :----------------------------------------------------------: | :----: | :-------------------------------------------------: |
| public Commit(String message, Blobs[] blobArray, Commit parent) |   /    | 根据给定的message,parent以及blobArray生成commit对象 |
|     public static byte[] readFileToBytes(File blob_file)     | byte[] |    给定文件对象，读取文件中的内容到byte[]后返回     |
|       public static String calculateID(byte[] content)       | String |         根据文件数据计算出相应的SHA-1哈希ID         |
|                 private String getBlobsID()                  | String |   获取所有blobArray中的blobID，被calculateID调用    |

## Algorithms

### Init

- 



## Persistence





