// Copyright (c) 2012 P. Taylor Goetz (ptgoetz@gmail.com)

package com.instanceone.hdfs.shell.command;

import java.io.IOException;
import java.net.URI;

import com.dstreev.hdfs.shell.command.Constants;
import com.instanceone.stemshell.command.AbstractCommand;
import jline.console.ConsoleReader;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

import org.apache.commons.cli.CommandLine;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.instanceone.stemshell.Environment;
import org.apache.hadoop.security.UserGroupInformation;

public class HdfsConnect extends AbstractCommand {

    public HdfsConnect(String name) {
        super(name);
        Completer completer = new StringsCompleter("hdfs://localhost:8020/", "hdfs://hdfshost:8020/");
        this.completer = completer;
    }

    public void execute(Environment env, CommandLine cmd, ConsoleReader reader) {
        try {
//            if(cmd.getArgs().length > 0){
            Configuration config = new Configuration();

            if (env.getProperty(com.instanceone.hdfs.shell.command.HdfsKrb.USE_KERBEROS) != null && env.getProperty(com.instanceone.hdfs.shell.command.HdfsKrb.USE_KERBEROS) == "true") {
                config.set(com.instanceone.hdfs.shell.command.HdfsKrb.HADOOP_AUTHENTICATION, com.instanceone.hdfs.shell.command.HdfsKrb.KERBEROS);
                config.set(com.instanceone.hdfs.shell.command.HdfsKrb.HADOOP_AUTHORIZATION, "true");
                config.set(com.instanceone.hdfs.shell.command.HdfsKrb.HADOOP_KERBEROS_NN_PRINCIPAL, env.getProperty(com.instanceone.hdfs.shell.command.HdfsKrb.HADOOP_KERBEROS_NN_PRINCIPAL));
                UserGroupInformation.setConfiguration(config);
            }

            FileSystem hdfs = null;
            try {
                if (cmd.getArgs().length > 0) {
                    hdfs = FileSystem.get(URI.create(cmd.getArgs()[0]), config);
                } else {
                    hdfs = FileSystem.get(config);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }

            env.setValue(Constants.CFG, config);
            env.setValue(Constants.HDFS, hdfs);
            // set working dir to root
            hdfs.setWorkingDirectory(hdfs.makeQualified(new Path("/")));

            FileSystem local = FileSystem.getLocal(new Configuration());
            env.setValue(Constants.LOCAL_FS, local);
            env.setProperty(Constants.HDFS_URL, hdfs.getUri().toString());

            FSUtil.prompt(env);

            log(cmd, "Connected: " + hdfs.getUri());
            logv(cmd, "HDFS CWD: " + hdfs.getWorkingDirectory());
            logv(cmd, "Local CWD: " + local.getWorkingDirectory());

//            }
        } catch (IOException e) {
            log(cmd, e.getMessage());
        }
    }

    @Override
    public Completer getCompleter() {
        return this.completer;
    }


}
