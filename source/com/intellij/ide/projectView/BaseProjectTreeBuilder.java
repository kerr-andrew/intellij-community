package com.intellij.ide.projectView;

import com.intellij.ide.favoritesTreeView.FavoritesTreeNodeDescriptor;
import com.intellij.ide.projectView.impl.ProjectAbstractTreeStructureBase;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.util.ui.tree.TreeUtil;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class BaseProjectTreeBuilder extends AbstractTreeBuilder {
  protected final Project myProject;

  public BaseProjectTreeBuilder(Project project, JTree tree, DefaultTreeModel treeModel, ProjectAbstractTreeStructureBase treeStructure, Comparator<NodeDescriptor> comparator) {
    super(tree, treeModel, treeStructure, comparator);
    myProject = project;
  }

  protected boolean isAlwaysShowPlus(NodeDescriptor nodeDescriptor) {
    return ((AbstractTreeNode)nodeDescriptor).isAlwaysShowPlus();
  }

  protected boolean isAutoExpandNode(NodeDescriptor nodeDescriptor) {
    return nodeDescriptor.getParentDescriptor() == null;
  }

  protected final void expandNodeChildren(final DefaultMutableTreeNode node) {
    Object element = ((NodeDescriptor)node.getUserObject()).getElement();
    VirtualFile[] virtualFiles = getFilesToRefresh(element);
    super.expandNodeChildren(node);
    for (int i = 0; i < virtualFiles.length; i++) {
      VirtualFile virtualFile = virtualFiles[i];
      virtualFile.refresh(true, false);
    }
  }

  protected static final VirtualFile[] getFilesToRefresh(Object element) {
    final VirtualFile virtualFile;
    if (element instanceof PsiDirectory){
      virtualFile = ((PsiDirectory)element).getVirtualFile();
    }
    else if (element instanceof PsiFile){
      virtualFile = ((PsiFile)element).getVirtualFile();
    }
    else{
      virtualFile = null;
    }
    return virtualFile != null ? new VirtualFile[]{virtualFile} : VirtualFile.EMPTY_ARRAY;
  }

  public List<AbstractTreeNode> getOrBuildChildren(AbstractTreeNode parent) {
    buildNodeForElement(parent);

    DefaultMutableTreeNode node = getNodeForElement(parent);
    //expandNodeChildren(node);

    if (node == null) {
      return new ArrayList<AbstractTreeNode>();
    }

    myTree.expandPath(new TreePath(node.getPath()));
    
    List<AbstractTreeNode> result = new ArrayList<AbstractTreeNode>();
    for (int i = 0; i < node.getChildCount(); i++) {
      javax.swing.tree.TreeNode childAt = node.getChildAt(i);
      DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode)childAt;
      if (defaultMutableTreeNode.getUserObject() instanceof AbstractTreeNode) {
        ProjectViewNode treeNode = (ProjectViewNode)defaultMutableTreeNode.getUserObject();
        result.add(treeNode);
      } else if (defaultMutableTreeNode.getUserObject() instanceof FavoritesTreeNodeDescriptor){
        AbstractTreeNode treeNode = ((FavoritesTreeNodeDescriptor)defaultMutableTreeNode.getUserObject()).getElement();
        result.add(treeNode);
      }

    }

    return result;
  }

  public void hideChildrenFor(DefaultMutableTreeNode node) {
    if (node != null){
      final JTree tree = getTree();
      final TreePath path = new TreePath(node.getPath());
      if (tree.isExpanded(path)) {
        tree.collapsePath(path);
      }
    }
  }

  public void select(Object element, VirtualFile file, boolean requestFocus) {
    AbstractTreeNode node = select((AbstractTreeNode)getTreeStructure().getRootElement(), file, element);
    TreeUtil.selectInTree(getNodeForElement(node), requestFocus, getTree());
  }

  private AbstractTreeNode select(AbstractTreeNode current, VirtualFile file, Object element) {
    if (Comparing.equal(current.getValue(), element)) return current;

    if (current instanceof ProjectViewNode && !(((ProjectViewNode)current).contains(file))) return null;

    DefaultMutableTreeNode currentNode = getNodeForElement(current);

    boolean expanded = currentNode == null ? false : getTree().isExpanded(new TreePath(currentNode.getPath()));

    List<AbstractTreeNode> kids = getOrBuildChildren(current);
    for (int i = 0; i < kids.size(); i++) {
      AbstractTreeNode node = kids.get(i);
      AbstractTreeNode result = select(node, file, element);
      if (result != null) {
        currentNode = getNodeForElement(current);
        if (currentNode != null) {
          final TreePath path = new TreePath(currentNode.getPath());
          if (!getTree().isExpanded(path)) {
            getTree().expandPath(path);
          }
        }
        return result;
      }
      else {
        if (!expanded) {
          hideChildrenFor(currentNode);
        }
      }
    }

    return null;
  }

}
