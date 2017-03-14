## Summary
一个基于scala.meta的轻量ddd框架

## Feature
- 使用[scala.meta](http://scalameta.org)用于仓储代码的宏展开, 减少仓储代码的编写, 关注领域逻辑
- 基于事件驱动, 易于与Akka等框架集成

## Usage
当前为试验版本, 需要编译, 由于使用了宏展开, 需要配合编译器插件[paradise](https://github.com/scalameta/paradise).
此为scala.meta下的分支

sbt配置参考`build.sbt`文件下关于testcase模块的配置

使用方式参考testcase模块下的例子, 命令行下`sbt test`即可执行测试查看结果

## Structure
`macros`模块下包含两个注解: `@EntityObject`和`@Repository`, 作为宏展开的标记.

`core`模块包含模型、仓储、事件相关的基本类(很多为抽象类), 以及一些这些类的简单实现(位于`com.zhranklin.ddd.support`包下).

### Event
事件方面, 主要为两个基类: `EventBus`和`EventSource`(特质), `EventBus`有方法`addSource(EventSource)`,
以监听该`EventSource`发出的消息. 而`EventSource`具有子类`EventSource.WithSender`以及相应的`Sender`类,
便于自己用来发送消息到总线.

### Persistence
持久化方面, 实体对象的持久化与加载, 经历三个流程:

1. 实体属性与数据库中属性的相互转化
2. 实体与DMO的相互转化
3. DMO的存取(至数据库)

DMO引用一个统一的case class:

```scala
case class Dmo[T](id: Id, table: String, attributes: Map[String, T])
```

id为实体id, table为表名, attributes为属性map, T则是可自定义用来统一标示数据库属性值的类, 可以是`String`, 某些复杂类,
甚至是`Any`, 取决于如何实现仓储.

Mapper类则是负责数据库的存取工作:

```scala
trait Mapper[T] {
  def read(id: Id, clazz: Class[_]): Dmo[T]

  def write(dmo: Dmo[T])
}
```

read方法中的`clazz`(可用ClassTag隐式参数获取)用于决定取数据的表名

实体属性与数据库中的属性, 涉及两个特质:

```scala
trait Marshaller[-A, +B] {
  def marshal(a: A): B
}

trait Unmarshaller[+A, -B] {
  def unmarshal(b: B): A
}
```

由于scala编译器的bug, `Unmarshaller`的泛型参数只能暂时使用`A`, 而不是`+A`,
否则会导致隐式推导时无法正常执行materialization(传入ClassTag参数)

`Marshaller`将实体属性转换成数据库属性, `Unmarshaller`则反之, 这里的两个特质中的`B`(数据库属性)即对应Dmo中的T

1、3两个流程都已经阐述清楚, 需要另行实现, 实体与DMO之间的转化则涉及到两个宏注解: `@EntityObject`与`@Repository`

### @EntityObject

举例, 考虑一个属性比较复杂的实体: 

```scala
@EntityObject
case class TestObj(a: String, b: Int, c: List[String], d: List[Map[Int, List[Option[String]]]])
```

展开后

```scala
case class TestObj(a: String, b: Int, c: List[String], d: List[Map[Int, List[Option[String]]]])(implicit sender: com.zhranklin.ddd.infra.event.Sender, id: com.zhranklin.ddd.model.Id[TestObj]) extends com.zhranklin.ddd.model.entityObject {
  import com.zhranklin.ddd.infra.event.Event.{Update, Delete}

  sender.send(Update(this))
  def update(a: String = this.a, b: Int = this.b, c: List[String] = this.c, d: List[Map[Int, List[Option[String]]]] = this.d) = {
    val updated = TestObj(a, b, c, d)
    sender.send(Update(updated))
    updated
  }
  def delete() = {
    sender.send(Delete(this))
  }
}

object TestObj {

  trait Repo {
    import com.zhranklin.ddd.infra.persistence.{Unmarshaller, Marshaller}
    import com.zhranklin.ddd.model.entityObject

    implicit def testObjToDmo[T](obj: TestObj)(implicit _mInt: Marshaller[Int, T], _mList_l_String_r_ : Marshaller[List[String], T], _mString: Marshaller[String, T], _mList_l_Map_l_Int_a_List_l_Option_l_String_r__r__r__r_ : Marshaller[List[Map[Int, List[Option[String]]]], T], sender: com.zhranklin.ddd.infra.event.Sender) = {
      val attr = Map[String, T](("a", _mString.marshal(obj.a)), ("b", _mInt.marshal(obj.b)), ("c", _mList_l_String_r_.marshal(obj.c)), ("d", _mList_l_Map_l_Int_a_List_l_Option_l_String_r__r__r__r_.marshal(obj.d)))
      Dmo[T](obj.id, "TestObj", attr)
    }

    implicit def testObjFromDmo[T](dmo: Dmo[T])(implicit _mInt: Unmarshaller[Int, T], _mList_l_String_r_ : Unmarshaller[List[String], T], _mString: Unmarshaller[String, T], _mList_l_Map_l_Int_a_List_l_Option_l_String_r__r__r__r_ : Unmarshaller[List[Map[Int, List[Option[String]]]], T], sender: com.zhranklin.ddd.infra.event.Sender) = {
      implicit val id = com.zhranklin.ddd.model.Id[TestObj](dmo.id.id)
      TestObj(a = _mString.unmarshal(dmo.attributes("a")), b = _mInt.unmarshal(dmo.attributes("b")), c = _mList_l_String_r_.unmarshal(dmo.attributes("c")), d = _mList_l_Map_l_Int_a_List_l_Option_l_String_r__r__r__r_.unmarshal(dmo.attributes("d")))
    }
  }

}
```

可以见得, 展开后, 实体类内的代码, 实现了在更新, 新建以及删除的时候, 使用隐式传递的`Sender`向事件总线发送相关消息.
而伴生对象中的Repo, 则提供了Dmo与该实体类的相互转换的隐式函数, 只要组合了这个trait, 不需要明确调用, 即可实现转换.
实体创建所需的隐式参数`Sender`与`Id`, 则在`DMCreationContext`中提供, 需要`IdGenerater`来提供id的产生方式,
如指定id(用于修改实体), 使用UUID生成id.

### @Repository
该注解则用于将之前所述各种半生对象中的Repo进行整合, 以及进行write的时候, 消除类型擦除的影响, 因为write方法为处理事件的时候调用,
此时其包含的实体由于类型擦除的关系, 已经无法直接得知其类型, 故在此进行类型判断:

展开前

```scala
@Repository
trait Repos extends RepoImplicits with WithRepos[(Parent, Simple, ttt.TestObj, ttt.Root)]
```

展开后

```scala
trait Repos extends RepoImplicits with Parent.Repo with Simple.Repo with ttt.TestObj.Repo with ttt.Root.Repo {
  import com.zhranklin.ddd.model.{entityObject, Id}

  implicit def read[T, E <: entityObject](id: Id[E])(implicit mapper: Mapper[T], f: Dmo[T] => E, classTag: scala.reflect.ClassTag[E]): E = mapper.read(id, classTag.runtimeClass)

  implicit private def writeGen[T, E <: entityObject](e: E)(implicit mapper: Mapper[T], f: E => Dmo[T]): Unit = mapper.write(e)

  val write: entityObject => Unit = {
    case e0: Parent => writeGen(e0)
    case e0: Simple => writeGen(e0)
    case e0: ttt.TestObj => writeGen(e0)
    case e0: ttt.Root => writeGen(e0)
  }
}
```

结果则是, Repo特质提供两个公有方法: read和write, 皆消除了类型擦除的影响.

## 下一步工作

- 完成角色、实体继承等领域模型的特性
- 着手与sql、nosql、Ehcache等实际工具的集成