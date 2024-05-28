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

|                            函数名                            |    返回值    |                             描述                             |
| :----------------------------------------------------------: | :----------: | :----------------------------------------------------------: |
|              public **Blobs**(String fileName)               |      /       |                 根据给定的文件名生成blob对象                 |
|   public static byte[] **readFileToBytes**(File blob_file)   |    byte[]    |         给定文件对象，读取文件中的内容到byte[]后返回         |
|     public static String **calculateID**(byte[] content)     |    String    |             根据文件数据计算出相应的SHA-1哈希ID              |
| public static boolean trackFiles(List<Blobs> previousBlobList, Blobs currentBlob) |   boolean    | 对比两个BlobsList是否一致，即检查上一个commit的BlobsList与当前要添加的blob是否一致 |
| public static List<Blobs\> returnBlobsList(List<String\> fileNames, File workStage) | List<Blobs\> |                        返回所有Blobs                         |
| public static boolean deleteStageFile(String fileName, String command, Blobs blobFile) |   boolean    |                删除或者添加STAGE_AREA中的blob                |
|                                                              |              |                                                              |



### Commit

每个commit代表一次提交，有一个独一无二的ID，提交信息，提交时间，对blobs的引用以及父亲commit

#### 属性

|  属性名   |     格式     |                  描述                   |
| :-------: | :----------: | :-------------------------------------: |
| commit_ID |    String    |    每个commit都有一个由SHA-1生成的ID    |
|  message  |    String    | 每次提交都会有一个message来描述本次提交 |
|   time    |     Date     |               提交的时间                |
| blobArray | List<Blobs\> |  本次提交所包含的blob，存储在此队列中   |
|  parent   |    commit    |          本次提交的父亲commit           |

#### 函数

|                            函数名                            |    返回值     |                        描述                         |
| :----------------------------------------------------------: | :-----------: | :-------------------------------------------------: |
| public **Commit**(String message, Blobs[] blobArray, Commit parent) |       /       | 根据给定的message,parent以及blobArray生成commit对象 |
|   public static byte[] **readFileToBytes**(File blob_file)   |    byte[]     |    给定文件对象，读取文件中的内容到byte[]后返回     |
|     public static String **calculateID**(byte[] content)     |    String     |         根据文件数据计算出相应的SHA-1哈希ID         |
|               private String **getBlobsID**()                |    String     |   获取所有blobArray中的blobID，被calculateID调用    |
|  public void **writeCommit**(File AREA, String commitName)   |       /       |             将此commit写入给定的AREA中              |
|   public void **clearStageArea**(List<String\> fileNames)    |       /       |           提交完commit后将STAGE_AREA清空            |
| public static boolean **updateBlobArray**(Commit updateCommit, List<Blobs\> previousBlobArray, List<String\> fileNames, String command) |    Boolean    |                更新Commit的BlobArray                |
| public static List<Commit\> **returnCommitList**(Commit currentCommit) | List<Commit\> |      根据父亲指针循环获取Commit得到CommitList       |



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



## IDEAL

1. public static boolean **updateBlobArray**(Commit updateCommit, List<Blobs\> previousBlobArray, List<String\> fileNames, String command)函数中，需要一个tempBlobArray来操作存储变化的新BlobArray，因为直接令tempBlobArray = previousBlobArray的话他们俩指向的是同一块地址，操作tempBlobArray时previousBlobArray指向的内容也变换导致所有commit的BlobArray也随之变换出现问题。
2. 用shortID查询commitID完成相应的`java gitlet.Main checkout [commit id] -- [file name]`功能会影响速度，Git的解决办法是把Blobs们依据**哈希值**的前两位建立文件夹进行存储，查找时只需算出哈希值就可以快速查找(O(1))。

