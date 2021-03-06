// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: BlockFooterProto.proto

package net.icircuit.bucketdb.models.proto;

public final class BlockFooterProto {
  private BlockFooterProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface BlockFooterOrBuilder extends
      // @@protoc_insertion_point(interface_extends:kvstorageengine.BlockFooter)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>int64 ribOffset = 1;</code>
     * @return The ribOffset.
     */
    long getRibOffset();

    /**
     * <code>int64 ribLength = 2;</code>
     * @return The ribLength.
     */
    long getRibLength();
  }
  /**
   * Protobuf type {@code kvstorageengine.BlockFooter}
   */
  public  static final class BlockFooter extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:kvstorageengine.BlockFooter)
      BlockFooterOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use BlockFooter.newBuilder() to construct.
    private BlockFooter(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private BlockFooter() {
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new BlockFooter();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private BlockFooter(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 8: {

              ribOffset_ = input.readInt64();
              break;
            }
            case 16: {

              ribLength_ = input.readInt64();
              break;
            }
            default: {
              if (!parseUnknownField(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return net.icircuit.bucketdb.models.proto.BlockFooterProto.internal_static_kvstorageengine_BlockFooter_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return net.icircuit.bucketdb.models.proto.BlockFooterProto.internal_static_kvstorageengine_BlockFooter_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter.class, net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter.Builder.class);
    }

    public static final int RIBOFFSET_FIELD_NUMBER = 1;
    private long ribOffset_;
    /**
     * <code>int64 ribOffset = 1;</code>
     * @return The ribOffset.
     */
    public long getRibOffset() {
      return ribOffset_;
    }

    public static final int RIBLENGTH_FIELD_NUMBER = 2;
    private long ribLength_;
    /**
     * <code>int64 ribLength = 2;</code>
     * @return The ribLength.
     */
    public long getRibLength() {
      return ribLength_;
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (ribOffset_ != 0L) {
        output.writeInt64(1, ribOffset_);
      }
      if (ribLength_ != 0L) {
        output.writeInt64(2, ribLength_);
      }
      unknownFields.writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (ribOffset_ != 0L) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt64Size(1, ribOffset_);
      }
      if (ribLength_ != 0L) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt64Size(2, ribLength_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter)) {
        return super.equals(obj);
      }
      net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter other = (net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter) obj;

      if (getRibOffset()
          != other.getRibOffset()) return false;
      if (getRibLength()
          != other.getRibLength()) return false;
      if (!unknownFields.equals(other.unknownFields)) return false;
      return true;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + RIBOFFSET_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
          getRibOffset());
      hash = (37 * hash) + RIBLENGTH_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
          getRibLength());
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code kvstorageengine.BlockFooter}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:kvstorageengine.BlockFooter)
        net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooterOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return net.icircuit.bucketdb.models.proto.BlockFooterProto.internal_static_kvstorageengine_BlockFooter_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return net.icircuit.bucketdb.models.proto.BlockFooterProto.internal_static_kvstorageengine_BlockFooter_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter.class, net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter.Builder.class);
      }

      // Construct using net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        ribOffset_ = 0L;

        ribLength_ = 0L;

        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return net.icircuit.bucketdb.models.proto.BlockFooterProto.internal_static_kvstorageengine_BlockFooter_descriptor;
      }

      @java.lang.Override
      public net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter getDefaultInstanceForType() {
        return net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter.getDefaultInstance();
      }

      @java.lang.Override
      public net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter build() {
        net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter buildPartial() {
        net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter result = new net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter(this);
        result.ribOffset_ = ribOffset_;
        result.ribLength_ = ribLength_;
        onBuilt();
        return result;
      }

      @java.lang.Override
      public Builder clone() {
        return super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter) {
          return mergeFrom((net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter other) {
        if (other == net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter.getDefaultInstance()) return this;
        if (other.getRibOffset() != 0L) {
          setRibOffset(other.getRibOffset());
        }
        if (other.getRibLength() != 0L) {
          setRibLength(other.getRibLength());
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private long ribOffset_ ;
      /**
       * <code>int64 ribOffset = 1;</code>
       * @return The ribOffset.
       */
      public long getRibOffset() {
        return ribOffset_;
      }
      /**
       * <code>int64 ribOffset = 1;</code>
       * @param value The ribOffset to set.
       * @return This builder for chaining.
       */
      public Builder setRibOffset(long value) {
        
        ribOffset_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>int64 ribOffset = 1;</code>
       * @return This builder for chaining.
       */
      public Builder clearRibOffset() {
        
        ribOffset_ = 0L;
        onChanged();
        return this;
      }

      private long ribLength_ ;
      /**
       * <code>int64 ribLength = 2;</code>
       * @return The ribLength.
       */
      public long getRibLength() {
        return ribLength_;
      }
      /**
       * <code>int64 ribLength = 2;</code>
       * @param value The ribLength to set.
       * @return This builder for chaining.
       */
      public Builder setRibLength(long value) {
        
        ribLength_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>int64 ribLength = 2;</code>
       * @return This builder for chaining.
       */
      public Builder clearRibLength() {
        
        ribLength_ = 0L;
        onChanged();
        return this;
      }
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:kvstorageengine.BlockFooter)
    }

    // @@protoc_insertion_point(class_scope:kvstorageengine.BlockFooter)
    private static final net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter();
    }

    public static net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<BlockFooter>
        PARSER = new com.google.protobuf.AbstractParser<BlockFooter>() {
      @java.lang.Override
      public BlockFooter parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new BlockFooter(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<BlockFooter> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<BlockFooter> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public net.icircuit.bucketdb.models.proto.BlockFooterProto.BlockFooter getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_kvstorageengine_BlockFooter_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_kvstorageengine_BlockFooter_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\026BlockFooterProto.proto\022\017kvstorageengin" +
      "e\"3\n\013BlockFooter\022\021\n\tribOffset\030\001 \001(\003\022\021\n\tr" +
      "ibLength\030\002 \001(\003B6\n\"net.icircuit.bucketdb." +
      "models.protoB\020BlockFooterProtob\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_kvstorageengine_BlockFooter_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_kvstorageengine_BlockFooter_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_kvstorageengine_BlockFooter_descriptor,
        new java.lang.String[] { "RibOffset", "RibLength", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
