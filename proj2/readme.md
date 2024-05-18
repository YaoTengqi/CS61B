# Gitlet
本项目是编写一个简易版git

## Classes and Data Structures
### Main
Main文件是整个项目的入口，他根据输入的args[]参数识别命令并执行。

### Blobs

每个blob代表一个文件，保存着文件的具体内容，也是gitlet处理的元数据。

#### 属性

|  属性名  |  格式  |                    描述                    |
| :------: | :----: | :----------------------------------------: |
|  blobID  | String |      每个blob都有一个由SHA-1生成的ID       |
| blobName | String |        每个blob都有一个自己的文件名        |
| content  | byte[] | 每个blob将存储文件的内容，压缩为byte[]格式 |

#### 函数

|                            函数名                            | 返回值  |                             描述                             |
| :----------------------------------------------------------: | :-----: | :----------------------------------------------------------: |
|              public **Blobs**(String fileName)               |    /    |                 根据给定的文件名生成blob对象                 |
|   public static byte[] **readFileToBytes**(File blob_file)   | byte[]  |         给定文件对象，读取文件中的内容到byte[]后返回         |
|     public static String **calculateID**(byte[] content)     | String  |             根据文件数据计算出相应的SHA-1哈希ID              |
|          public boolean **equals**(Blobs... blobs)           | boolean |                      对比Blobs是否相同                       |
| public static Blobs[] returnBlobsArray(List<String> fileNames) | Blobs[] |                        返回所有Blobs                         |
| public static void **deleteStageFile**(File createFile, String fileName) |    /    |                    删除STAGE_AREA中的blob                    |
| public static Blobs[] updateBlobArray(Blobs[] previousBlobArray, List<String> fileNames,) | Blobs[] | 处理List<String>中的fileNames并根据内容进行更新，返回更新后的blobArray |



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
| public **Commit**(String message, Blobs[] blobArray, Commit parent) |   /    | 根据给定的message,parent以及blobArray生成commit对象 |
|   public static byte[] **readFileToBytes**(File blob_file)   | byte[] |    给定文件对象，读取文件中的内容到byte[]后返回     |
|     public static String **calculateID**(byte[] content)     | String |         根据文件数据计算出相应的SHA-1哈希ID         |
|               private String **getBlobsID**()                | String |   获取所有blobArray中的blobID，被calculateID调用    |
|  public void **writeCommit**(File AREA, String commitName)   |   /    |             将此commit写入给定的AREA中              |
|    public void **clearStageArea**(List<String> fileNames)    |   /    |           提交完commit后将STAGE_AREA清空            |



### Repository

Repository负责对文件夹进行操作

#### 属性

|   属性名    |    格式    |                 描述                  |
| :---------: | :--------: | :-----------------------------------: |
|     CWD     | final File |          当前工作的文件路径           |
| GITLET_DIR  | final File |             .gitlet的路径             |
| STAGE_AREA  | final File | 暂存区路径，用于存储add指令添加的文件 |
| COMMIT_AREA | fina File  |       用于存储每次commit的对象        |
|  HEAD_AREA  | fina File  |     用于存储每次head头指针的对象      |

#### 函数

|               函数名                | 返回值 |         描述          |
| :---------------------------------: | :----: | :-------------------: |
|   public static void makeSetup()    |   /    |  初始化.gitlet文件夹  |
| public static void makeStageArea()  |   /    | 创建STAGE_AREA文件夹  |
| public static void makeCommitArea() |   /    | 创建COMMIT_AREA文件夹 |

## Algorithms

### Init

- 



## Persistence





